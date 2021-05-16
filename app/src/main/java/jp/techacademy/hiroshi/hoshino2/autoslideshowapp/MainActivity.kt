package jp.techacademy.hiroshi.hoshino2.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.graphics.drawable.Drawable
import android.net.Uri
import android.net.Uri.fromFile
import android.os.Handler
import androidx.core.net.toFile
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import com.google.android.material.snackbar.Snackbar
import android.view.View

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val listuri =  mutableListOf<Uri>()
        var index = 0
        var playorstop = 0
        var mTimer: Timer? = null
        val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目（null = 全項目）
                null, // フィルタ条件（null = フィルタなし）
                null, // フィルタ用パラメータ
                null // ソート (nullソートなし）
        )


        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                listuri.add(imageUri)
                // Log.d("ANDROID", "URI : " + imageUri.toString())

            } while (cursor.moveToNext())
        }
        cursor.close()

        val size = listuri.size

        if (size == 0) {
            val rootLayout: View = findViewById(android.R.id.content)
            val snackbar = Snackbar.make(rootLayout, "ギャラリーに画像を保存してください！", Snackbar.LENGTH_LONG)
            snackbar.show()
        } else {

            // Log.d("Hoshino", "size : $size")
            imageView.setImageURI(listuri[index])

            forward_button.setOnClickListener {
                if (playorstop == 0) {
                    index += 1
                    if (index >= size) {
                        index = index - size
                    }

                    // Log.d("Hoshino_forward0", "index : $index")
                    // Log.d("Hoshino_forward1", "URI : $listuri[index]")
                    imageView.setImageURI(listuri[index])
                }
            }

            reverse_button.setOnClickListener {
                if (playorstop == 0) {
                    index -= 1
                    if (index < 0) {
                        index = index + size
                    }
                    // Log.d("Hoshino_reverse0", "index : $index")
                    // Log.d("Hoshino_reverse1", "URI : $listuri[index]")
                    imageView.setImageURI(listuri[index])
                }
            }

            play_button.setOnClickListener {

                // Log.d("Hoshino_play push0", "$playorstop")

                if (playorstop == 0) {
                    // Log.d("Hoshino_play push1", "$playorstop")

                    var mTimerSec = 0
                    var mHandler = Handler()
                    play_button.text = "停止"
                    playorstop = 1

                    if (mTimer == null) {
                        mTimer = Timer()
                        mTimer!!.schedule(object : TimerTask() {
                            override fun run() {

                                // Log.d("Hoshino_play push2", "$playorstop")

                                mTimerSec += 2
                                mHandler.post {
                                    index += 1
                                    if (index >= size) {
                                        index = index - size
                                    }
                                    // Log.d("Hoshino_timer", "index : $mTimerSec")
                                    // Log.d("Hoshino_play0", "index : $index")
                                    // Log.d("Hoshino_play1", "URI : $listuri[index]")
                                    imageView.setImageURI(listuri[index])
                                    // commentout Log.d("HOSHINO0", String.format("%.0f", mTimerSec))
                                }
                            }
                        }, 2000, 2000) // 最初に始動させるまで2000ミリ秒、ループの間隔を2000ミリ秒 に設定
                    }


                } else {
                    // Log.d("Hoshino_play push3", "$playorstop")
                    if (mTimer != null) {
                        mTimer!!.cancel()
                        mTimer = null
                    }
                    playorstop = 0
                    play_button.text = "再生"
                    imageView.setImageURI(listuri[index])
                    // Log.d("Hoshino_play push4", "$playorstop")
                }

            }
        }
    }
}