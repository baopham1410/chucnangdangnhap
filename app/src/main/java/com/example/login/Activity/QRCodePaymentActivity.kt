package com.example.login.Activity

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView // Thêm import này
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.login.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import android.util.Log
import android.content.Intent
import com.example.login.Model.CartItem // Đảm bảo CartItem Model ở đúng package
import com.google.zxing.BarcodeFormat // Thêm import này
import com.google.zxing.MultiFormatWriter // Thêm import này
import com.google.zxing.WriterException // Thêm import này
import java.text.NumberFormat // Thêm import này
import java.util.Locale // Thêm import này

class QRCodePaymentActivity : AppCompatActivity() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var paymentDoneButton: MaterialButton
    private lateinit var totalAmountTextView: TextView // Thêm TextView để hiển thị tổng tiền
    private lateinit var orderIdTextView: TextView     // Thêm TextView để hiển thị mã đơn hàng
    private lateinit var bankInfoTextView: TextView    // Thêm TextView để hiển thị thông tin ngân hàng

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thanhtoanqr) // Đảm bảo tên layout này là chính xác

        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        paymentDoneButton = findViewById(R.id.paymentDoneButton)
        totalAmountTextView = findViewById(R.id.totalAmountTextView) // Ánh xạ TextView
        orderIdTextView = findViewById(R.id.orderIdTextView)         // Ánh xạ TextView
        bankInfoTextView = findViewById(R.id.bankInfoTextView)       // Ánh xạ TextView

        // Lấy dữ liệu từ Intent
        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)
        val orderId = intent.getStringExtra("orderId") ?: "N/A"

        val bankName = "VIETCOMBANK"
        val accountNumber = "1025766045"
        val accountHolder = "PHAM QUOC BAO"
        val branch = "CN HCM"

        // Tạo chuỗi nội dung cho mã QR
        // Đây là ví dụ về định dạng VietQR (chuẩn QR Code do NHNN Việt Nam ban hành)
        // Lưu ý: Để QR này có thể quét được bởi các ứng dụng ngân hàng, nó phải tuân thủ chuẩn.
        // Chuẩn VietQR đầy đủ có thể phức tạp hơn, thường yêu cầu API từ ngân hàng hoặc trung gian.
        // Đây là một phiên bản đơn giản hóa hoặc nếu bạn muốn tạo QR không theo chuẩn VietQR mà chỉ chứa thông tin.
        val qrContent = "bankCode=${bankName}&accountNo=${accountNumber}&amount=${totalAmount.toLong()}&addInfo=DH${orderId}&template=Compact2022"
        // Hoặc một chuỗi nội dung đơn giản dễ đọc cho người dùng nếu không phải là VietQR chính thức:
        // val qrContent = "Ngân hàng: $bankName\n" +
        //                 "Số tài khoản: $accountNumber\n" +
        //                 "Chủ tài khoản: $accountHolder\n" +
        //                 "Số tiền: ${formatPrice(totalAmount)}\n" +
        //                 "Nội dung: DH${orderId}"

        try {
            // Tạo mã QR
            val qrCodeBitmap = generateQRCode(qrContent, 500, 500) // Kích thước QR (px)
            qrCodeImageView.setImageBitmap(qrCodeBitmap)

            // Hiển thị thông tin tổng tiền, mã đơn hàng và thông tin ngân hàng lên TextViews
            totalAmountTextView.text = "Tổng tiền: ${formatPrice(totalAmount)}"
            orderIdTextView.text = "Mã đơn hàng: $orderId"
            bankInfoTextView.text = "Ngân hàng: $bankName\nSố TK: $accountNumber\nChủ TK: $accountHolder\nNội dung CK: DH${orderId}"

        } catch (e: WriterException) {
            Log.e("QRCodePaymentActivity", "Lỗi khi tạo mã QR", e)
            Toast.makeText(this, "Lỗi khi tạo mã QR: ${e.message}", Toast.LENGTH_LONG).show()
            // Có thể hiển thị hình ảnh lỗi hoặc ẩn ImageView
            qrCodeImageView.setImageResource(R.drawable.welcome) // Giả sử bạn có icon lỗi
        }

        paymentDoneButton.setOnClickListener {
            if (!orderId.isNullOrEmpty()) {
                updatePaymentStatus(orderId)
            } else {
                Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Hàm tạo mã QR (COPY TỪ MAINACTIVITY CỦA BẠN HOẶC MỘT UTILITY FILE)
    private fun generateQRCode(text: String, width: Int, height: Int): Bitmap {
        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        return bitmap
    }

    // Hàm format giá (COPY TỪ MAINACTIVITY CỦA BẠN HOẶC MỘT UTILITY FILE)
    private fun formatPrice(price: Double): String {
        return currencyFormatter.format(price)
    }


    // Hàm này giữ nguyên logic cập nhật trạng thái thanh toán và xóa giỏ hàng
    private fun updatePaymentStatus(orderId: String) {
        firestore.collection("checkouts")
            .whereEqualTo("orderId", orderId)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentId = querySnapshot.documents[0].id
                    firestore.collection("checkouts")
                        .document(documentId)
                        .update("paymentStatus", "Đã thanh toán đang chờ xác nhận")
                        .addOnSuccessListener {
                            Toast.makeText(this, "Đã xác nhận thanh toán. Vui lòng chờ xử lý đơn hàng.", Toast.LENGTH_LONG).show()
                            // Không xóa giỏ hàng ở đây vì nó đã được xử lý trong CheckoutActivity
                            // Nếu bạn muốn giỏ hàng được xóa TẠI ĐÂY (sau khi người dùng nhấn "Payment Done"),
                            // bạn phải xóa logic cập nhật số lượng kho và xóa giỏ hàng khỏi CheckoutActivity.
                            // Hiện tại, logic của bạn đã xử lý việc đó ở CheckoutActivity.
                            // Vì vậy, chúng ta sẽ bỏ qua hàm completeOrderAndClearCart() ở đây.
                            // Thay vào đó, chỉ cần chuyển về HomeActivity.
                            val intent = Intent(this@QRCodePaymentActivity, ProfileActivity::class.java)
                            startActivity(intent)
                            finishAffinity() // Kết thúc tất cả các Activity trước đó
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Lỗi khi cập nhật trạng thái thanh toán", e)
                            Toast.makeText(this, "Lỗi khi xác nhận thanh toán", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Không tìm thấy đơn hàng với mã: $orderId", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi khi truy vấn đơn hàng", e)
                Toast.makeText(this, "Lỗi khi kiểm tra đơn hàng", Toast.LENGTH_SHORT).show()
            }
    }

    // Hàm này (completeOrderAndClearCart) không nên được gọi từ đây nếu bạn đã xử lý nó trong CheckoutActivity.
    // Nếu bạn muốn nó được xử lý SAU KHI ng dùng nhấn "Payment Done" trong QR Activity,
    // thì bạn phải BỎ logic đó khỏi CheckoutActivity và gọi nó TẠI ĐÂY.
    // Dựa trên luồng hiện tại của bạn, nó đã được xử lý trong CheckoutActivity, vì vậy hãy xóa hoặc không gọi hàm này.
    /*
    private fun completeOrderAndClearCart() {
        currentUser?.uid?.let { userId ->
            firestore.collection("cart_items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val cartBatch = firestore.batch()
                    val cartItemsListForUpdate = mutableListOf<CartItem>()
                    for (document in querySnapshot) {
                        val cartItem = document.toObject(CartItem::class.java)
                        cartItemsListForUpdate.add(cartItem)
                        cartBatch.delete(document.reference)
                    }

                    val batchUpdateStock = firestore.batch()
                    for (cartItem in cartItemsListForUpdate) {
                        val drinkDocumentRef = firestore.collection("drink").document(cartItem.drinkId)
                        batchUpdateStock.update(
                            drinkDocumentRef,
                            "quantity",
                            FieldValue.increment(-cartItem.quantity.toLong())
                        )
                    }

                    batchUpdateStock.commit()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Đã cập nhật số lượng kho")
                            cartBatch.commit()
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Giỏ hàng đã được xóa")
                                    val intent = Intent(this@QRCodePaymentActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finishAffinity()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Lỗi khi xóa giỏ hàng", e)
                                    Toast.makeText(this@QRCodePaymentActivity, "Lỗi khi xóa giỏ hàng", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Lỗi khi cập nhật số lượng kho", e)
                            Toast.makeText(this@QRCodePaymentActivity, "Lỗi khi cập nhật kho", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Lỗi khi truy vấn giỏ hàng", e)
                    Toast.makeText(this@QRCodePaymentActivity, "Lỗi khi lấy giỏ hàng", Toast.LENGTH_SHORT).show()
                }
        }
    }
    */
}