import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import toast from 'react-hot-toast';

const schema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  email: z.string().email('Enter a valid email'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string(),
}).refine((d) => d.password === d.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

type FormData = z.infer<typeof schema>;

export function RegisterPage() {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    try {
      await registerUser({ firstName: data.firstName, lastName: data.lastName, email: data.email, password: data.password });
      toast.success('Account created! Welcome aboard.');
      navigate('/dashboard');
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Registration failed');
    }
  };

  const fields = [
    { name: 'firstName' as const, label: 'First Name', type: 'text', placeholder: 'John' },
    { name: 'lastName' as const, label: 'Last Name', type: 'text', placeholder: 'Doe' },
    { name: 'email' as const, label: 'Email', type: 'email', placeholder: 'you@example.com' },
    { name: 'password' as const, label: 'Password', type: 'password', placeholder: '8+ characters' },
    { name: 'confirmPassword' as const, label: 'Confirm Password', type: 'password', placeholder: '••••••••' },
  ];

  return (
    <div>
      <h2 className="text-2xl font-bold text-white">Create account</h2>
      <p className="mt-2 text-slate-400 text-sm">Start tracking corporate events for free</p>

      <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-4">
        <div className="grid grid-cols-2 gap-4">
          {fields.slice(0, 2).map(({ name, label, type, placeholder }) => (
            <div key={name}>
              <label className="block text-sm font-medium text-slate-300 mb-1.5">{label}</label>
              <input
                {...register(name)}
                type={type}
                placeholder={placeholder}
                className="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
              {errors[name] && <p className="mt-1 text-xs text-red-400">{errors[name]?.message}</p>}
            </div>
          ))}
        </div>

        {fields.slice(2).map(({ name, label, type, placeholder }) => (
          <div key={name}>
            <label className="block text-sm font-medium text-slate-300 mb-1.5">{label}</label>
            <input
              {...register(name)}
              type={type}
              placeholder={placeholder}
              className="w-full px-4 py-3 bg-slate-800 border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
            />
            {errors[name] && <p className="mt-1 text-xs text-red-400">{errors[name]?.message}</p>}
          </div>
        ))}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full py-3 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-semibold rounded-lg transition-colors mt-2"
        >
          {isSubmitting ? 'Creating account...' : 'Create Account'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-400">
        Already have an account?{' '}
        <Link to="/login" className="text-blue-400 hover:text-blue-300 font-medium">Sign in</Link>
      </p>
    </div>
  );
}
