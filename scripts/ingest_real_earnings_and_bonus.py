import os
import sys
import hashlib
import json
from datetime import datetime, timezone
import psycopg2
import yfinance as yf

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "corporate_actions_db")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASS = os.getenv("DB_PASSWORD", "postgres")

def get_db():
    return psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASS
    )

def main():
    conn = get_db()
    cur = conn.cursor()

    cur.execute("SELECT id, symbol FROM companies WHERE active = TRUE;")
    companies = cur.fetchall()
    print(f"Found {len(companies)} active companies.")

    inserted_earnings = 0
    updated_bonus = 0

    # 1. Update Reliance & Wipro 2:1 stock splits to BONUS_ISSUE if they were bonus issues
    cur.execute("""
        UPDATE corporate_actions 
        SET event_type = 'BONUS_ISSUE'::event_type,
            description = 'Bonus Issue 1:1'
        WHERE source_record_id IN ('YF_SPLIT_RELIANCE_1730087100', 'YF_SPLIT_WIPRO_1733197500');
    """)
    updated_bonus = cur.rowcount
    conn.commit()
    print(f"Updated {updated_bonus} records to BONUS_ISSUE.")

    # 2. Ingest Real Quarterly Financial Results (EARNINGS) from Yahoo Finance
    for company_id, symbol in companies:
        ticker_symbol = f"{symbol}.NS"
        print(f"Fetching earnings dates for {symbol} ({ticker_symbol})...")
        try:
            t = yf.Ticker(ticker_symbol)
            ed = t.earnings_dates
            if ed is None or ed.empty:
                continue

            # Take the 8 most recent / upcoming quarterly earnings
            for date_idx, row in ed.head(8).iterrows():
                earn_dt = date_idx.to_pydatetime()
                earn_date = earn_dt.date()
                timestamp = int(earn_dt.timestamp())
                source_record_id = f"YF_EARN_{symbol}_{timestamp}"

                # Calculate quarter and FY
                month = earn_date.month
                year = earn_date.year
                if month in [1, 2, 3]:
                    quarter = "Q3"
                    fy = f"FY{year}"
                elif month in [4, 5, 6]:
                    quarter = "Q4"
                    fy = f"FY{year}"
                elif month in [7, 8, 9]:
                    quarter = "Q1"
                    fy = f"FY{year + 1}"
                else:
                    quarter = "Q2"
                    fy = f"FY{year + 1}"

                eps_est = row.get("EPS Estimate")
                eps_rep = row.get("Reported EPS")
                desc_parts = [f"Quarterly Financial Results - {quarter} {fy}"]
                if eps_rep is not None and not (isinstance(eps_rep, float) and eps_rep != eps_rep):
                    desc_parts.append(f"Reported EPS: INR {eps_rep:.2f}")
                elif eps_est is not None and not (isinstance(eps_est, float) and eps_est != eps_est):
                    desc_parts.append(f"EPS Estimate: INR {eps_est:.2f}")

                description = " | ".join(desc_parts)

                raw_payload = json.dumps({
                    "date": str(earn_date),
                    "timestamp": timestamp,
                    "eps_estimate": float(eps_est) if eps_est is not None and not (isinstance(eps_est, float) and eps_est != eps_est) else None,
                    "reported_eps": float(eps_rep) if eps_rep is not None and not (isinstance(eps_rep, float) and eps_rep != eps_rep) else None
                })
                checksum = hashlib.sha256(raw_payload.encode()).hexdigest()

                # Store raw market data
                cur.execute("""
                    INSERT INTO raw_market_data (source, source_record_id, company_symbol, payload_json, checksum, processed, processed_at)
                    VALUES (%s, %s, %s, %s, %s, TRUE, NOW())
                    ON CONFLICT (source, checksum) DO NOTHING;
                """, ("YAHOO_FINANCE", source_record_id, symbol, raw_payload, checksum))

                # Store corporate action
                cur.execute("""
                    INSERT INTO corporate_actions (
                        company_id, event_type, announcement_date, effective_date,
                        quarter, financial_year, description, status, data_quality,
                        source, source_record_id, source_reference, last_verified_at,
                        created_at, updated_at
                    ) VALUES (
                        %s, 'EARNINGS'::event_type, %s, %s,
                        %s, %s, %s, 'ANNOUNCED'::corporate_action_status, 'VERIFIED'::data_quality_status,
                        'YAHOO_FINANCE', %s, %s, NOW(),
                        NOW(), NOW()
                    )
                    ON CONFLICT (source, source_record_id) DO UPDATE SET
                        effective_date = EXCLUDED.effective_date,
                        description = EXCLUDED.description,
                        last_verified_at = NOW(),
                        updated_at = NOW();
                """, (
                    company_id, earn_date, earn_date,
                    quarter, fy, description,
                    source_record_id, f"YAHOO_FINANCE:{source_record_id}"
                ))
                inserted_earnings += 1

            conn.commit()
        except Exception as e:
            print(f"Error fetching for {symbol}: {e}")
            conn.rollback()

    cur.close()
    conn.close()
    print(f"Ingestion complete: {inserted_earnings} EARNINGS records processed!")

if __name__ == "__main__":
    main()
