//package com.iyuce.itoefl.UI.Listening.Fragment;
//
//import android.database.sqlite.SQLiteDatabase;
//import android.graphics.Color;
//import android.media.MediaPlayer;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.iyuce.itoefl.Common.Constants;
//import com.iyuce.itoefl.R;
//import com.iyuce.itoefl.UI.Listening.Adapter.QuestionAdapter;
//import com.iyuce.itoefl.Utils.DbUtil;
//import com.iyuce.itoefl.Utils.LogUtil;
//import com.iyuce.itoefl.Utils.TimeUtil;
//import com.iyuce.itoefl.Utils.ToastUtil;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//
//public class FragmentDoQuestion extends FragmentDoQuestionDefault implements QuestionAdapter.OnQuestionItemClickListener,
//        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
//
//    private TextView mTxtCurrentQuestion, mTxtTotalQuestion, mTxtQuestionContent;
//    private TextView mTxtProgressCurrent, mTxtProgressTotal;
//    private ProgressBar mProgressBar;
//    //可选视图
//    private RelativeLayout mRelativeLayout;
//
//    //答题选项
//    private RecyclerView mRecyclerView;
//    private ArrayList<String> mOptionContentList = new ArrayList<>();
//    private ArrayList<String> mOptionCodeList = new ArrayList<>();
//    private QuestionAdapter mAdapter;
//
//    private MediaPlayer mMediaPlayer;
//
//    //用于不止一段音频的题型
//    private boolean isOnlyAudio = true;
//    private int mEndPosition = 0;
//    private String mEndText;
//
//    //提供给Activity用于判断是否播放录音完毕
//    private boolean isFinish = true;
//    //提供给Activity一个默认答案，如果为空则未答完，不让进入下一题
//    private String answerDefault;
//
//    //接收参数
//    private String total_question, current_question, current_music, current_question_id, local_path, local_paper_code;
//
//    //查表所得的属性
//    private String mQuestionType, mContent, mAnswer;
//
//    private Handler mMediaProgressHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Message message = Message.obtain();
//            message.what = Constants.FLAG_AUDIO_PLAY;
//            mMediaProgressHandler.sendMessageDelayed(message, 1000);
//            getCurrent();
//        }
//    };
//
//    public String selectAnswer() {
//        return answerDefault;
//    }
//
//    public boolean finishMediaPlayer() {
//        return isFinish;
//    }
//
//    public static FragmentDoQuestion newInstance(String total_question, String current_question,
//                                                 String current_music, String current_question_id,
//                                                 String local_path, String local_paper_code) {
//        FragmentDoQuestion fragment = new FragmentDoQuestion();
//        Bundle args = new Bundle();
//        args.putString("total_question", total_question);
//        args.putString("current_question", current_question);
//        args.putString("current_music", current_music);
//        args.putString("current_question_id", current_question_id);
//        args.putString("local_path", local_path);
//        args.putString("local_paper_code", local_paper_code);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            total_question = getArguments().getString("total_question");
//            current_question = getArguments().getString("current_question");
//            current_music = getArguments().getString("current_music");
//            current_question_id = getArguments().getString("current_question_id");
//            local_path = getArguments().getString("local_path");
//            local_paper_code = getArguments().getString("local_paper_code");
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mMediaPlayer.release();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        mMediaPlayer.stop();
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_listen_do_question, container, false);
//
//        initView(view);
//        return view;
//    }
//
//    private void initView(View view) {
//        //数据源
//        SQLiteDatabase mDatabase = DbUtil.getHelper(getActivity(), local_path + "/" + local_paper_code + ".sqlite").getWritableDatabase();
//        //TODO 直接传递来就好，不用再重复查表Question
//        mContent = DbUtil.queryToString(mDatabase, Constants.TABLE_QUESTION, Constants.Content, Constants.ID, current_question_id);
//        mQuestionType = DbUtil.queryToString(mDatabase, Constants.TABLE_QUESTION, Constants.QuestionType, Constants.ID, current_question_id);
//        mAnswer = DbUtil.queryToString(mDatabase, Constants.TABLE_QUESTION, Constants.Answer, Constants.ID, current_question_id);
//        //查表Option //TODO 判断MasterId是否为null，不为null则需要查表Child,此处查不同表依靠同一个questionId
//        mOptionContentList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_OPTION, Constants.Content, Constants.QuestionId + " =? ", current_question_id);
//        mOptionCodeList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_OPTION, Constants.Code, Constants.QuestionId + " =? ", current_question_id);
//        mDatabase.close();
//
//        mTxtCurrentQuestion = (TextView) view.findViewById(R.id.txt_fragment_do_result_page_middle);
//        mTxtTotalQuestion = (TextView) view.findViewById(R.id.txt_fragment_do_result_page_right);
//        mTxtQuestionContent = (TextView) view.findViewById(R.id.txt_fragment_do_result_title);
//        mTxtProgressCurrent = (TextView) view.findViewById(R.id.txt_fragment_do_question_current);
//        mTxtProgressTotal = (TextView) view.findViewById(R.id.txt_fragment_do_question_total);
//        mProgressBar = (ProgressBar) view.findViewById(R.id.bar_fragment_do_question_progress);
//
//        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_fragment_do_result);
//        mRelativeLayout = (RelativeLayout) view.findViewById(R.id.relative_fragment_do_result_page);
//        if (!mQuestionType.equals("多音频题")) {
//            mRelativeLayout.setVisibility(View.GONE);
//        } else {
//            //多音频题
//            isOnlyAudio = false;
//            ToastUtil.showMessage(getActivity(), "本题是多录音题");
//        }
//
//        //TODO  这里模拟控制切换多选题和判断题或者其他题型
//        if (mQuestionType.equals("")) {
//            mQuestionType = Constants.QUESTION_TYPE_JUDGE;
//        }
//        if (TextUtils.equals(mQuestionType, Constants.QUESTION_TYPE_SINGEL)) {
//            //单选题
//        } else if (TextUtils.equals(mQuestionType, Constants.QUESTION_TYPE_JUDGE)) {
//            //判断题
//        } else if (TextUtils.equals(mQuestionType, Constants.QUESTION_TYPE_MULTI)) {
//            //多选题
//        }
//
//        //布置参数到对应控件
//        mTxtCurrentQuestion.setText(current_question);
//        mTxtTotalQuestion.setText(total_question);
//        mTxtQuestionContent.setText(mContent);
//
//        //TODO 没有录音，权宜之计，放在外面做,本来应该放在录音播放完毕时
//        mAdapter = new QuestionAdapter(getActivity(), mOptionContentList, mQuestionType);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mAdapter.setOnQuestionItemClickListener(this);
//        mRecyclerView.setAdapter(mAdapter);
//
//        //MediaPlayer
//        mMediaPlayer = new MediaPlayer();
//        mMediaPlayer.setOnPreparedListener(this);
//        mMediaPlayer.setOnErrorListener(this);
//        mMediaPlayer.setOnCompletionListener(this);
//        try {
//            String musicPath = local_path + File.separator + current_music;
//            mMediaPlayer.setDataSource(musicPath);
//            mMediaPlayer.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //MediaPlayer
//    @Override
//    public void onCompletion(MediaPlayer mp) {
//        if (!isOnlyAudio) {
//            isOnlyAudio = true;
//            //避免音频再次播放时，延迟1秒的handle持续更新进度条
//            mMediaProgressHandler.removeMessages(Constants.FLAG_AUDIO_PLAY);
//            mProgressBar.setProgress(mEndPosition);
//            mTxtProgressCurrent.setText(mEndText);
//            mMediaPlayer.reset();
//            try {
//                String musicPath = local_path + File.separator + current_music;
//                mMediaPlayer.setDataSource(musicPath);
//                mMediaPlayer.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return;
//        }
////        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
////        mAdapter = new QuestionAdapter(getActivity(), mOptionContentList, mQuestionType);
////        mAdapter.setOnQuestionItemClickListener(this);
////        mRecyclerView.setAdapter(mAdapter);
//        isFinish = true;
//    }
//
//    @Override
//    public boolean onError(MediaPlayer mp, int what, int extra) {
//        mp.reset();
//        return false;
//    }
//
//    @Override
//    public void onPrepared(MediaPlayer mp) {
//        mMediaPlayer.start();
//        if (!isOnlyAudio) {
//            Message msg = Message.obtain();
//            msg.what = Constants.FLAG_AUDIO_PLAY;
//            mMediaProgressHandler.sendMessage(msg);
//            getDrution();
//        }
//    }
//
//    //音频进度
//    private void getCurrent() {
//        mProgressBar.setProgress(mMediaPlayer.getCurrentPosition());
//        int time = mMediaPlayer.getCurrentPosition() / 1000;
//        String timer = TimeUtil.toTimeShow(time);
//        mTxtProgressCurrent.setText(timer);
//    }
//
//    private void getDrution() {
//        mEndPosition = mMediaPlayer.getDuration();
//        mProgressBar.setMax(mEndPosition);
//        int time = mMediaPlayer.getDuration() / 1000;
//        String timer = TimeUtil.toTimeShow(time);
//        mTxtProgressTotal.setText(timer);
//        mEndText = timer;
//    }
//
//    //Adapter提供给Fragment的方法
//    @Override
//    public void onQuestionClick(int pos) {
//        if (TextUtils.equals(mQuestionType, Constants.QUESTION_TYPE_SINGEL)) {
//            //单选
//            answerDefault = mOptionCodeList.get(pos);
//            resetItemSelectStyle(pos);
//        } else if (TextUtils.equals(mQuestionType, Constants.QUESTION_TYPE_JUDGE)) {
//            //判断
//            answerDefault = mAdapter.returnSelectList().toString();
//        } else if (TextUtils.equals(mQuestionType, Constants.QUESTION_TYPE_MULTI)) {
//            //多选
//            answerDefault = "";
//            ArrayList mList = mAdapter.returnSelectList();
//            for (int i = 0; i < mList.size(); i++) {
//                if ((boolean) mList.get(i)) {
//                    answerDefault = answerDefault + i;
//                }
//            }
//        } else {
//            //TODO留坑,可以删，给非以上题型默认为单选题
//            answerDefault = mOptionCodeList.get(pos);
//            resetItemSelectStyle(pos);
//        }
//        LogUtil.i("mAnswer = " + mAnswer + ",,and you choose " + answerDefault);
//    }
//
//    /**
//     * 单选题时重设选中的Item及全部的Item
//     */
//    private void resetItemSelectStyle(int pos) {
//        TextView textView;
//        for (int i = 0; i < mOptionContentList.size(); i++) {
//            if (pos == i) {
//                textView = (TextView) mRecyclerView.getChildAt(pos).findViewById(R.id.txt_item_fragment_do_question);
//                textView.setBackgroundResource(R.drawable.view_bound_orange_stroke);
//                continue;
//            }
//            textView = (TextView) mRecyclerView.getChildAt(i).findViewById(R.id.txt_item_fragment_do_question);
//            textView.setBackgroundColor(Color.parseColor("#ffffff"));
//        }
//    }
//}