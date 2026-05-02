import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { productService, categoryService, inventoryService } from '../services'
import { Plus, Package, Layers, BarChart2 } from 'lucide-react'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'

// ─── Tab: Products ───────────────────────────────────────────

function ProductsTab() {
  const queryClient = useQueryClient()
  const [showForm, setShowForm] = useState(false)
  const { register, handleSubmit, reset, formState: { errors } } = useForm()

  const { data, isLoading } = useQuery({
    queryKey: ['admin-products'],
    queryFn: () => productService.getAll({ page: 0, size: 50 }),
  })

  const { data: catData } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryService.getAll(),
  })

  const createMutation = useMutation({
    mutationFn: (data) => productService.create({
      ...data,
      price: parseFloat(data.price),
      originalPrice: data.originalPrice ? parseFloat(data.originalPrice) : undefined,
      categoryId: parseInt(data.categoryId),
    }),
    onSuccess: () => {
      toast.success('Product created!')
      queryClient.invalidateQueries({ queryKey: ['admin-products'] })
      reset()
      setShowForm(false)
    },
    onError: (err) => toast.error(err.response?.data?.message ?? 'Create failed'),
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => productService.delete(id),
    onSuccess: () => {
      toast.success('Product deleted')
      queryClient.invalidateQueries({ queryKey: ['admin-products'] })
    },
  })

  const products   = data?.data?.data?.content ?? []
  const categories = catData?.data?.data ?? []

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-bold text-gray-900">Products ({products.length})</h2>
        <button onClick={() => setShowForm(!showForm)}
          className="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2 rounded-lg
                     text-sm font-medium hover:bg-indigo-700">
          <Plus className="w-4 h-4" /> New Product
        </button>
      </div>

      {/* Create form */}
      {showForm && (
        <form onSubmit={handleSubmit((d) => createMutation.mutate(d))}
          className="bg-indigo-50 border border-indigo-100 rounded-xl p-5 mb-6 grid grid-cols-2 gap-4">
          <h3 className="col-span-2 font-semibold text-gray-900">Create New Product</h3>

          {[
            { name: 'name',     label: 'Product Name', required: true },
            { name: 'sku',      label: 'SKU',           required: true },
            { name: 'price',    label: 'Price (₹)',     required: true, type: 'number' },
            { name: 'originalPrice', label: 'Original Price (₹)', type: 'number' },
            { name: 'brand',    label: 'Brand' },
            { name: 'imageUrl', label: 'Image URL' },
          ].map(({ name, label, required, type = 'text' }) => (
            <div key={name}>
              <label className="block text-xs font-medium text-gray-700 mb-1">{label}</label>
              <input {...register(name, { required })} type={type}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm
                           focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>
          ))}

          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">Category *</label>
            <select {...register('categoryId', { required: true })}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm
                         focus:outline-none focus:ring-2 focus:ring-indigo-500">
              <option value="">Select category</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>

          <div className="col-span-2">
            <label className="block text-xs font-medium text-gray-700 mb-1">Description</label>
            <textarea {...register('description')} rows={3}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm
                         focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>

          <div className="col-span-2 flex gap-3">
            <button type="submit" disabled={createMutation.isPending}
              className="bg-indigo-600 text-white px-5 py-2 rounded-lg text-sm font-medium
                         hover:bg-indigo-700 disabled:opacity-60">
              {createMutation.isPending ? 'Creating…' : 'Create Product'}
            </button>
            <button type="button" onClick={() => setShowForm(false)}
              className="border border-gray-300 text-gray-700 px-5 py-2 rounded-lg text-sm hover:bg-gray-50">
              Cancel
            </button>
          </div>
        </form>
      )}

      {/* Products table */}
      {isLoading ? (
        <div className="text-center py-10 text-gray-400">Loading products…</div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                {['Name', 'SKU', 'Price', 'Category', 'Status', 'Actions'].map(h => (
                  <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {products.map(p => (
                <tr key={p.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium text-gray-900 max-w-[200px] truncate">{p.name}</td>
                  <td className="px-4 py-3 text-gray-500 font-mono text-xs">{p.sku}</td>
                  <td className="px-4 py-3 font-semibold text-indigo-700">
                    ₹{Number(p.price).toLocaleString('en-IN')}
                  </td>
                  <td className="px-4 py-3 text-gray-500">{p.categoryName}</td>
                  <td className="px-4 py-3">
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium
                      ${p.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                      {p.status}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => { if (confirm('Delete this product?')) deleteMutation.mutate(p.id) }}
                      className="text-red-500 hover:text-red-700 text-xs font-medium">
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {products.length === 0 && (
            <div className="text-center py-10 text-gray-400">No products found</div>
          )}
        </div>
      )}
    </div>
  )
}

// ─── Tab: Inventory ───────────────────────────────────────────

function InventoryTab() {
  const { register, handleSubmit, reset } = useForm()
  const queryClient = useQueryClient()

  const upsertMutation = useMutation({
    mutationFn: (data) => inventoryService.upsert({
      ...data,
      productId: parseInt(data.productId),
      quantity: parseInt(data.quantity),
      lowStockThreshold: data.lowStockThreshold ? parseInt(data.lowStockThreshold) : 10,
    }),
    onSuccess: () => { toast.success('Inventory updated!'); reset() },
    onError: (err) => toast.error(err.response?.data?.message ?? 'Update failed'),
  })

  return (
    <div>
      <h2 className="text-lg font-bold text-gray-900 mb-4">Manage Inventory</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-gray-100 p-5">
          <h3 className="font-semibold text-gray-900 mb-4">Set / Update Stock</h3>
          <form onSubmit={handleSubmit((d) => upsertMutation.mutate(d))} className="space-y-4">
            {[
              { name: 'productId',         label: 'Product ID',           required: true, type: 'number' },
              { name: 'productSku',         label: 'Product SKU',          required: true },
              { name: 'quantity',           label: 'Quantity',             required: true, type: 'number' },
              { name: 'lowStockThreshold',  label: 'Low Stock Threshold',  type: 'number' },
              { name: 'warehouseLocation',  label: 'Warehouse Location' },
            ].map(({ name, label, required, type = 'text' }) => (
              <div key={name}>
                <label className="block text-xs font-medium text-gray-700 mb-1">{label}</label>
                <input {...register(name, { required })} type={type}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm
                             focus:outline-none focus:ring-2 focus:ring-indigo-500" />
              </div>
            ))}
            <button type="submit" disabled={upsertMutation.isPending}
              className="w-full bg-indigo-600 text-white py-2.5 rounded-lg text-sm font-medium
                         hover:bg-indigo-700 disabled:opacity-60">
              {upsertMutation.isPending ? 'Saving…' : 'Save Inventory'}
            </button>
          </form>
        </div>
        <div className="bg-indigo-50 rounded-xl border border-indigo-100 p-5">
          <h3 className="font-semibold text-gray-900 mb-3">Quick Tips</h3>
          <ul className="text-sm text-gray-600 space-y-2">
            <li>• Enter the Product ID from the Products tab</li>
            <li>• Default low-stock threshold is 10 units</li>
            <li>• System auto-alerts when stock falls below threshold</li>
            <li>• Reserved quantity is managed automatically by the Order saga</li>
            <li>• Use Restock API to add units to existing inventory</li>
          </ul>
        </div>
      </div>
    </div>
  )
}

// ─── Main Admin Page ───────────────────────────────────────────

const TABS = [
  { id: 'products',  label: 'Products',  icon: Package },
  { id: 'inventory', label: 'Inventory', icon: Layers  },
]

export default function AdminPage() {
  const [activeTab, setActiveTab] = useState('products')

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-sm text-gray-500 mt-1">Manage products, inventory and orders</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 border-b border-gray-200 mb-6">
        {TABS.map(({ id, label, icon: Icon }) => (
          <button key={id} onClick={() => setActiveTab(id)}
            className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium border-b-2 transition-colors
              ${activeTab === id
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
            <Icon className="w-4 h-4" />
            {label}
          </button>
        ))}
      </div>

      {activeTab === 'products'  && <ProductsTab />}
      {activeTab === 'inventory' && <InventoryTab />}
    </div>
  )
}
