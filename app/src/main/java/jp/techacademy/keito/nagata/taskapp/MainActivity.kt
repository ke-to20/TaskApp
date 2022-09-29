package jp.techacademy.keito.nagata.taskapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import android.view.View


const val EXTRA_TASK = "jp.techacademy.keito.nagata.taskapp.TASK"

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mRealm: Realm
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button1.setOnClickListener(this)

        fab.setOnClickListener { view ->
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
        }

        // Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this)

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        val searchText = search_edit_text.text.toString()
        Log.d("TaskApp", search_edit_text.text.length.toString())
        Log.d("TaskApp", searchText)


        reloadListView()
    }

    private fun reloadListView() {
        // Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults =
            mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        Log.d("TaskApp", "reloadListView()　が実行したよ")

//        val searchText = search_edit_text.text.toString()
//
//        Log.d("TaskApp", taskRealmResults.toString())
//        Log.d("TaskApp", taskRealmResults.javaClass.name)


        // 上記の結果を、TaskListとしてセットする
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }

    override fun onClick(v: View?) {
        Log.d("TaskApp", "ボタンが押されました")

        val searchText = search_edit_text.text.toString()

        val searchTextLenght = search_edit_text.text.length

        Log.d("TaskApp", search_edit_text.text.length.toString())
        Log.d("TaskApp", searchText)

        if (searchTextLenght != 0) {
            Log.d("TaskApp", "0文字ではなかった！")
            Log.d("TaskApp", searchTextLenght.toString() + "文字")
            Log.d("TaskApp", searchText)

            // Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
//            val taskRealmResults =
//                mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

//            category 内を検索
            var taskRealmResults = mRealm.where(Task::class.java)
                .equalTo("category", searchText)
                .findAll()

            // 上記の結果を、TaskListとしてセットする
            mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)

            // TaskのListView用のアダプタに渡す
            listView1.adapter = mTaskAdapter

            // 表示を更新するために、アダプターにデータが変更されたことを知らせる
            mTaskAdapter.notifyDataSetChanged()


        } else {
            Log.d("TaskApp", "0文字だわ！")
            reloadListView()

        }


    }


}