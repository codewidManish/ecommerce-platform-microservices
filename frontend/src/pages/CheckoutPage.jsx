import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate } from 'react-router-dom'
import { useCartStore } from '../store/cartStore'
import { orderService } from '../services'
import { useState } from 'react'
import toast from 'react-hot-toast'

const schema = z.object({
  fullName:       z.string().min(2, 'Required'),
  streetAddress:  z.string().min(5, 'Required'),
  city:           z.string().min(2, 'Required'),
  state:          z.string().min(2, 'Required'),
  postalCode:     z.string().regex(/^\d{6}$/, '6-digit PIN required'),
  country:        z.string().default('IN'),
  phone:          z.string().regex(/^\+?[0-9]{10,15}$/, 'Valid phone required'),
})

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { cart, clearCart } = useCartStore()
  const [isPlacing, setIsPlacing] = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { country: 'IN' },
  })

  if (!cart || cart.items.length === 0) {
    navigate('/cart')
    return null
  }

  const tax      = cart.totalAmount * 0.18
  const shipping = cart.totalAmount >= 500 ? 0 : 50
  const total    = cart.totalAmount + tax + shipping

  const onSubmit = async (addressData) => {
    setIsPlacing(true)
    try {
      const orderPayload = {
        items: cart.items.map(item => ({
          productId:   item.productId,
          productName: item.productName,
          productSku:  item.productSku,
          imageUrl:    item.imageUrl,
          quantity:    item.quantity,
          unitPrice:   item.unitPrice,
        })),
        shippingAddress: addressData,
      }

      const { data } = await orderService.placeOrder(orderPayload)
      if (data.success) {
        await clearCart()
        toast.success('Order placed successfully!')
        navigate(`/orders/${data.data.id}`)
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to place order')
    } finally {
      setIsPlacing(false)
    }
  }

  const Field = ({ name, label, placeholder, half }) => (
    <div className={half ? 'col-span-1' : 'col-span-2'}>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input {...register(name)} placeholder={placeholder}
        className="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm
                   focus:outline-none focus:ring-2 focus:ring-indigo-500" />
      {errors[name] && <p className="mt-1 text-xs text-red-500">{errors[name].message}</p>}
    </div>
  )

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Checkout</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Shipping Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="lg:col-span-2 bg-white rounded-xl border border-gray-100 p-6">
          <h2 className="font-bold text-gray-900 mb-4">Shipping Address</h2>
          <div className="grid grid-cols-2 gap-4">
            <Field name="fullName"      label="Full Name"      placeholder="John Doe" />
            <Field name="phone"         label="Phone"          placeholder="+919876543210" />
            <Field name="streetAddress" label="Street Address" placeholder="123 MG Road" />
            <Field name="city"          label="City"    placeholder="Bangalore" half />
            <Field name="state"         label="State"   placeholder="Karnataka" half />
            <Field name="postalCode"    label="PIN Code" placeholder="560001" half />
            <Field name="country"       label="Country"  placeholder="IN"       half />
          </div>
          <button type="submit" disabled={isPlacing}
            className="mt-6 w-full bg-indigo-600 text-white py-3 rounded-xl font-semibold
                       hover:bg-indigo-700 disabled:opacity-60 transition-colors">
            {isPlacing ? 'Placing Order…' : 'Place Order'}
          </button>
        </form>

        {/* Order Summary */}
        <div className="bg-white rounded-xl border border-gray-100 p-5 h-fit sticky top-24">
          <h2 className="font-bold text-gray-900 mb-4">Order Summary</h2>
          <div className="space-y-2.5 text-sm">
            {cart.items.map(item => (
              <div key={item.productId} className="flex justify-between text-gray-600">
                <span className="truncate max-w-[160px]">{item.productName} ×{item.quantity}</span>
                <span>₹{Number(item.subtotal).toLocaleString('en-IN')}</span>
              </div>
            ))}
            <div className="border-t border-gray-100 pt-2.5 space-y-2">
              <div className="flex justify-between text-gray-500">
                <span>Subtotal</span><span>₹{Number(cart.totalAmount).toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between text-gray-500">
                <span>Tax (18%)</span><span>₹{Math.round(tax).toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between text-gray-500">
                <span>Shipping</span>
                <span className={shipping === 0 ? 'text-green-600' : ''}>
                  {shipping === 0 ? 'Free' : `₹${shipping}`}
                </span>
              </div>
              <div className="flex justify-between font-bold text-gray-900 text-base pt-1 border-t border-gray-100">
                <span>Total</span><span>₹{Math.round(total).toLocaleString('en-IN')}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
