package jp.techacademy.takumi.sakurai.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.provider.MediaStore
import android.content.ContentUris
import android.util.Log
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.material.snackbar.Snackbar
import java.util.*

class MainActivity : AppCompatActivity(),  View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var mTimer: Timer? = null
    private var mHandler = Handler()

    val uriList = arrayListOf<String>()
    var maxImageUriIndexNum = 0
    var imageUriIndexNum = 0

    var isStarted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている

                // 画像の情報を取得する
                val resolver = contentResolver
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
                        val imageUri =
                            ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id
                            )

                        uriList.add(imageUri.toString())

                    } while (cursor.moveToNext())

                    imageView.setImageURI(uriList[0].toUri())
                    maxImageUriIndexNum = uriList.size - 1
                    imageNumber.text = (imageUriIndexNum+1).toString() + " / " + uriList.size.toString()
                }
                cursor.close()

                next_button.setOnClickListener(this)
                back_button.setOnClickListener(this)
                start_button.setOnClickListener(this)
                permission_button.visibility = View.INVISIBLE

            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            next_button.setOnClickListener(this)
            back_button.setOnClickListener(this)
            start_button.setOnClickListener(this)
            permission_button.visibility = View.INVISIBLE
        }
    }

    override fun onClick(v: View) {

        when (v.id) {
            R.id.next_button -> controlContents("next")
            R.id.back_button -> controlContents("back")
            R.id.start_button -> controlContents("start")
            R.id.permission_button -> controlContents("permission")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可された
                    // 画像の情報を取得する
                    val resolver = contentResolver
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
                            val imageUri =
                                ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )

                            uriList.add(imageUri.toString())

                        } while (cursor.moveToNext())

                        imageView.setImageURI(uriList[0].toUri())
                        maxImageUriIndexNum = uriList.size - 1
                        imageNumber.text = (imageUriIndexNum+1).toString() + " / " + uriList.size.toString()
                    }
                    cursor.close()

                    next_button.setOnClickListener(this)
                    back_button.setOnClickListener(this)
                    start_button.setOnClickListener(this)
                    permission_button.visibility = View.INVISIBLE
                    next_button.isEnabled = true
                    back_button.isEnabled = true
                    start_button.isEnabled = true
                } else {
                    // 許可されなかった
                    next_button.isEnabled = false
                    back_button.isEnabled = false
                    start_button.isEnabled = false
                    permission_button.setOnClickListener(this)

                    val rootLayout: View = findViewById(android.R.id.content)
                    val snackbar = Snackbar.make(rootLayout , "パーミッションが許可されていません。", Snackbar.LENGTH_LONG)
                    snackbar.show()
                }
        }
    }


    private fun controlContents(control: String) {
        when (control) {
            "permission"->{
                requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_CODE)
                }

            "next" -> {
                if (imageUriIndexNum == maxImageUriIndexNum) {
                    imageUriIndexNum = 0
                } else {
                    imageUriIndexNum += 1
                }
                imageView.setImageURI(uriList[imageUriIndexNum].toUri())
                imageNumber.text = (imageUriIndexNum+1).toString() + " / " + uriList.size.toString()
            }

            "back" -> {
                if (imageUriIndexNum == 0) {
                    imageUriIndexNum = maxImageUriIndexNum
                } else {
                    imageUriIndexNum -= 1
                }
                imageView.setImageURI(uriList[imageUriIndexNum].toUri())
                imageNumber.text = (imageUriIndexNum+1).toString() + " / " + uriList.size.toString()
            }

            "start" -> {
                if (isStarted) {
                    if (mTimer != null) {
                        mTimer!!.cancel()
                        mTimer = null
                    }
                    isStarted = false
                    next_button.isEnabled = true
                    back_button.isEnabled = true
                    start_button.text = "再生"
                } else {
                    if (mTimer == null) {
                        mTimer = Timer()
                        mTimer!!.schedule(object : TimerTask() {
                            override fun run() {
                                mHandler.post {
                                    if (imageUriIndexNum == maxImageUriIndexNum) {
                                        imageUriIndexNum = 0
                                    } else {
                                        imageUriIndexNum += 1
                                    }
                                    imageView.setImageURI(uriList[imageUriIndexNum].toUri())
                                    imageNumber.text = (imageUriIndexNum+1).toString() + " / " + uriList.size.toString()
                                }
                            }
                        }, 2000, 2000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
                    }
                    next_button.isEnabled = false
                    back_button.isEnabled = false
                    start_button.text = "停止"
                    isStarted = true
                }
            }
        }
    }
}