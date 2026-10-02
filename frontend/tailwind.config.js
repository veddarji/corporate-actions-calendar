/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        event: {
          dividend: '#22c55e',
          split: '#3b82f6',
          bonus: '#eab308',
          rights: '#a855f7',
          earnings: '#f97316',
          agm: '#64748b',
          buyback: '#ef4444',
          other: '#6b7280'
        }
      }
    },
  },
  plugins: [],
}
