import { useCartStore } from '../store/cartStore'
import { Link, useNavigate } from 'react-router-dom'
import { Trash2, Plus, Minus, ShoppingBag } from 'lucide-react'

export default function CartPage() {
  const { cart, removeItem, updateQuantity } = useCartStore()
  const navigate = useNavigate()

  if (!cart || cart.items.length === 0) {
    return (
      <div className="text-center py-20">
        <ShoppingBag className="w-16 h-16 text-gray-200 mx-auto mb-4" />
        <h2 className="text-xl font-bold text-gray-900">Your cart is empty</h2>
        <p className="text-gray-500 mt-2 mb-6">Start shopping to add items here</p>
        <Link to="/products"
          className="bg-indigo-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-indigo-700">
          Browse Products
        </Link>
      </div>
    )
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">
        Shopping Cart ({cart.totalItems} items)
      </h1>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Items */}
        <div className="lg:col-span-2 space-y-4">
          {cart.items.map((item) => (
            <div key={item.productId}
              className="bg-white rounded-xl border border-gray-100 p-4 flex gap-4 items-center">
              <img
                src={item.imageUrl || `https://placehold.co/80x80/e0e7ff/4f46e5?text=P`}
                alt={item.productName}
                className="w-20 h-20 rounded-lg object-cover shrink-0 bg-gray-50"
              />
              <div className="flex-1 min-w-0">
                <h3 className="font-medium text-gray-900 text-sm truncate">{item.productName}</h3>
                <p className="text-xs text-gray-400 mt-0.5">SKU: {item.productSku}</p>
                <p className="text-indigo-600 font-semibold mt-1">
                  ₹{Number(item.unitPrice).toLocaleString('en-IN')}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <button onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                  className="w-8 h-8 rounded-lg border border-gray-200 flex items-center justify-center hover:bg-gray-50">
                  <Minus className="w-3.5 h-3.5" />
                </button>
                <span className="w-8 text-center text-sm font-semibold">{item.quantity}</span>
                <button onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                  className="w-8 h-8 rounded-lg border border-gray-200 flex items-center justify-center hover:bg-gray-50">
                  <Plus className="w-3.5 h-3.5" />
                </button>
              </div>
              <div className="text-right min-w-[80px]">
                <p className="font-bold text-gray-900">₹{Number(item.subtotal).toLocaleString('en-IN')}</p>
                <button onClick={() => removeItem(item.productId)}
                  className="text-red-400 hover:text-red-600 mt-1">
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Summary */}
        <div>
          <div className="bg-white rounded-xl border border-gray-100 p-5 sticky top-24">
            <h3 className="font-bold text-gray-900 mb-4">Order Summary</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between text-gray-600">
                <span>Subtotal</span>
                <span>₹{Number(cart.totalAmount).toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Shipping</span>
                <span className="text-green-600">Free</span>
              </div>
              <div className="border-t border-gray-100 pt-2 mt-2 flex justify-between font-bold text-gray-900 text-base">
                <span>Total</span>
                <span>₹{Number(cart.totalAmount).toLocaleString('en-IN')}</span>
              </div>
            </div>
            <button onClick={() => navigate('/checkout')}
              className="w-full bg-indigo-600 text-white py-3 rounded-xl font-semibold mt-5
                         hover:bg-indigo-700 transition-colors">
              Proceed to Checkout
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
