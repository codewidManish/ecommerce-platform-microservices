import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

const schema = z.object({
  username:  z.string().min(3, 'Min 3 characters').max(50).regex(/^[a-zA-Z0-9_]+$/, 'Letters, digits and _ only'),
  email:     z.string().email('Invalid email'),
  password:  z.string().min(8, 'Min 8 characters')
               .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/, 'Must include upper, lower, digit, special char'),
  firstName: z.string().min(1, 'Required').max(50),
  lastName:  z.string().min(1, 'Required').max(50),
  phoneNumber: z.string().regex(/^\+?[0-9]{10,15}$/, 'Invalid phone').optional().or(z.literal('')),
})

export default function RegisterPage() {
  const navigate = useNavigate()
  const { register: registerUser, isLoading } = useAuthStore()

  const { register, handleSubmit, formState: { errors }, setError } = useForm({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data) => {
    const result = await registerUser(data)
    if (result.success) {
      navigate('/')
    } else {
      setError('root', { message: result.error })
    }
  }

  const Field = ({ name, label, type = 'text', placeholder }) => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input {...register(name)} type={type} placeholder={placeholder}
        className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm
                   focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent" />
      {errors[name] && <p className="mt-1 text-xs text-red-500">{errors[name].message}</p>}
    </div>
  )

  return (
    <div className="min-h-[80vh] flex items-center justify-center py-8">
      <div className="w-full max-w-lg">
        <div className="bg-white rounded-2xl shadow-lg border border-gray-100 p-8">
          <div className="text-center mb-6">
            <h1 className="text-2xl font-bold text-gray-900">Create Account</h1>
            <p className="text-gray-500 text-sm mt-1">Join ShopEasy today</p>
          </div>

          {errors.root && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
              {errors.root.message}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Field name="firstName" label="First Name" placeholder="John" />
              <Field name="lastName"  label="Last Name"  placeholder="Doe"  />
            </div>
            <Field name="username" label="Username" placeholder="johndoe" />
            <Field name="email"    label="Email"    type="email" placeholder="john@example.com" />
            <Field name="password" label="Password" type="password" placeholder="Min 8 chars, mixed case + symbol" />
            <Field name="phoneNumber" label="Phone (optional)" placeholder="+919876543210" />

            <button type="submit" disabled={isLoading}
              className="w-full bg-indigo-600 text-white py-2.5 rounded-lg text-sm font-semibold
                         hover:bg-indigo-700 disabled:opacity-60 transition-colors mt-2">
              {isLoading ? 'Creating account...' : 'Create Account'}
            </button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-5">
            Already have an account?{' '}
            <Link to="/login" className="text-indigo-600 font-medium hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
