import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CartViewModel : ViewModel() {
    private val _cartItems = MutableLiveData<List<Product>>().apply { value = emptyList() }
    val cartItems: LiveData<List<Product>> = _cartItems

    private val _cartItemCount = MutableLiveData<Int>().apply { value = 0 }
    val cartItemCount: LiveData<Int> = _cartItemCount

    fun addItem(product: Product) {
        val currentItems = _cartItems.value.orEmpty().toMutableList()
        currentItems.add(product)
        _cartItems.value = currentItems
        _cartItemCount.value = currentItems.size
    }

    fun removeItem(product: Product) {
        val currentItems = _cartItems.value.orEmpty().toMutableList()
        currentItems.remove(product)
        _cartItems.value = currentItems
        _cartItemCount.value = currentItems.size
    }

    // Các phương thức khác để quản lý giỏ hàng
}

data class Product(val id: String, val name: String) // Ví dụ về lớp Product