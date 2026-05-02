import { useQuery, useMutation } from '@tanstack/react-query'
import { useParams, Link } from 'react-router-dom'
import { orderService } from '../services'
import { useAuthStore } from '../store/authStore'
import { ArrowLeft, Package } from 'lucide-react'
import toast from 'react-hot-toast'

const STATUS_COLORS = {
  PENDING:    'bg-yellow-100 text-yellow-700 border-yellow-200',
  CONFIRMED:  'bg-blue-100 text-blue-700 border-blue-200',
  PROCESSING: 'bg-purple-100 text-purple-700 border-purple-200',
  SHIPPED:    'bg-indigo-100 text-indigo-700 border-indigo-200',
  DELIVERED:  'bg-green-100 text-green-700 border-green-200',
  CANCELLED:  'bg-red-100 text-red-700 border-red-200',
  REFUNDED:   'bg-gray-100 text-gray-600 border-gray-200',
}

export default function OrderPage() {
  const { id } = useParams()
  const { user } = useAuthStore()

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['order', id],
    queryFn: () => orderService.getOrder(id),
  })

  const cancelMutation = useMutation({
    mutationFn: () => orderService.cancelOrder(id, 'Cancelled by customer'),
    onSuccess: () => { toast.success('Order cancelled'); refetch() },
    onError: (err) => toast.error(err.response?.data?.message ?? 'Cancel failed'),
  })

  const order = data?.data?.data

  if (isLoading) {
    return (
      <div className="space-y-4 animate-pulse">
        <div className="h-6 bg-gray-200 rounded w-1/3" />
        <div className="h-40 bg-gray-200 rounded-xl" />
        <div className="h-40 bg-gray-200 rounded-xl" />
      </div>
    )
  }

  if (!order) {
    return (
      <div className="text-center py-20">
        <Package className="w-16 h-16 text-gray-200 mx-auto mb-4" />
        <h2 className="text-xl font-bold text-gray-900">Order not found</h2>
        <Link to="/orders" className="text-indigo-600 hover:underline mt-2 block">Back to orders</Link>
      </div>
    )
  }

  const canCancel = ['PENDING', 'CONFIRMED'].includes(order.status)

  return (
    <div>
      <Link to="/orders" className="flex items-center gap-1 text-sm text-gray-500 hover:text-indigo-600 mb-6">
        <ArrowLeft className="w-4 h-4" /> Back to orders
      </Link>

      <div className="flex items-start justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{order.orderNumber}</h1>
          <p className="text-sm text-gray-500 mt-1">
            Placed on {new Date(order.createdAt).toLocaleDateString('en-IN', {
              day: 'numeric', month: 'long', year: 'numeric'
            })}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className={`text-sm px-3 py-1 rounded-full font-medium border
            ${STATUS_COLORS[order.status] ?? 'bg-gray-100 text-gray-600'}`}>
            {order.status}
          </span>
          {canCancel && (
            <button
              onClick={() => cancelMutation.mutate()}
              disabled={cancelMutation.isPending}
              className="text-sm text-red-600 border border-red-200 px-3 py-1 rounded-full
                         hover:bg-red-50 transition-colors disabled:opacity-60">
              Cancel Order
            </button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Items */}
        <div className="lg:col-span-2 space-y-3">
          <h2 className="font-bold text-gray-900">Items Ordered</h2>
          {order.items?.map((item) => (
            <div key={item.productId}
              className="bg-white rounded-xl border border-gray-100 p-4 flex gap-4">
              <img
                src={item.imageUrl || `https://placehold.co/64x64/e0e7ff/4f46e5?text=P`}
                alt={item.productName}
                className="w-16 h-16 rounded-lg object-cover bg-gray-50"
              />
              <div className="flex-1">
                <p className="font-medium text-gray-900 text-sm">{item.productName}</p>
                <p className="text-xs text-gray-400">SKU: {item.productSku}</p>
                <p className="text-xs text-gray-500 mt-1">
                  {item.quantity} × ₹{Number(item.unitPrice).toLocaleString('en-IN')}
                </p>
              </div>
              <p className="font-bold text-gray-900">
                ₹{Number(item.subtotal).toLocaleString('en-IN')}
              </p>
            </div>
          ))}
        </div>

        {/* Summary */}
        <div className="space-y-4">
          <div className="bg-white rounded-xl border border-gray-100 p-4">
            <h3 className="font-bold text-gray-900 mb-3">Price Breakdown</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between text-gray-600">
                <span>Subtotal</span>
                <span>₹{Number(order.subtotal).toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Tax</span>
                <span>₹{Number(order.taxAmount).toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Shipping</span>
                <span>{Number(order.shippingAmount) === 0 ? 'Free' : `₹${Number(order.shippingAmount).toLocaleString('en-IN')}`}</span>
              </div>
              <div className="border-t pt-2 flex justify-between font-bold text-gray-900">
                <span>Total</span>
                <span>₹{Number(order.totalAmount).toLocaleString('en-IN')}</span>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl border border-gray-100 p-4">
            <h3 className="font-bold text-gray-900 mb-2">Payment</h3>
            <span className={`text-xs px-2 py-0.5 rounded-full font-medium
              ${order.paymentStatus === 'PAID' ? 'bg-green-100 text-green-700' :
                order.paymentStatus === 'FAILED' ? 'bg-red-100 text-red-700' :
                'bg-yellow-100 text-yellow-700'}`}>
              {order.paymentStatus}
            </span>
          </div>
        </div>
      </div>
    </div>
  )
}
