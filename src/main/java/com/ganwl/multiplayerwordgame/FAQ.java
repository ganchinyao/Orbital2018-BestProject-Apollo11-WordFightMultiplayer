package com.ganwl.multiplayerwordgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ganwl.multiplayerwordgame.helper.SoundPoolManager;

import net.cachapa.expandablelayout.ExpandableLayout;

public class FAQ extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faq);

        findViewById(R.id.faq_backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final RelativeLayout faq_questionArray[] = {(RelativeLayout) findViewById(R.id.faq_question1), (RelativeLayout) findViewById(R.id.faq_question2),
                (RelativeLayout) findViewById(R.id.faq_question3), (RelativeLayout) findViewById(R.id.faq_question4),
                (RelativeLayout) findViewById(R.id.faq_question5), (RelativeLayout) findViewById(R.id.faq_question6), findViewById(R.id.faq_question7)};


        final ExpandableLayout faq_answerArray[] = {(ExpandableLayout) findViewById(R.id.faq_answer1), (ExpandableLayout) findViewById(R.id.faq_answer2),
                (ExpandableLayout) findViewById(R.id.faq_answer3), (ExpandableLayout) findViewById(R.id.faq_answer4),
                (ExpandableLayout) findViewById(R.id.faq_answer5), (ExpandableLayout) findViewById(R.id.faq_answer6), findViewById(R.id.faq_answer7)
        };

        final ImageView arrowViewArray[] = {(ImageView) findViewById(R.id.faq_qn1ImageView), (ImageView) findViewById(R.id.faq_qn2ImageView),
                (ImageView) findViewById(R.id.faq_qn3ImageView), (ImageView) findViewById(R.id.faq_qn4ImageView),
                (ImageView) findViewById(R.id.faq_qn5ImageView), (ImageView) findViewById(R.id.faq_qn6ImageView), findViewById(R.id.faq_qn7ImageView)};

        for (int i = 0; i < faq_questionArray.length; i++) {
            final int temp = i;
            faq_questionArray[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (faq_answerArray[temp].isExpanded()) {
                        // close the expandable
                        SoundPoolManager.getInstance().playSound(5);
                        faq_answerArray[temp].collapse();
                        faq_questionArray[temp].setBackgroundResource(R.drawable.faqbottomlineonlybackground);
                        arrowViewArray[temp].setImageResource(R.drawable.faqarrow_down);
                    } else {
                        // open the expandable
                        SoundPoolManager.getInstance().playSound(4);
                        faq_answerArray[temp].expand();
                        faq_questionArray[temp].setBackground(null);
                        arrowViewArray[temp].setImageResource(R.drawable.faqarrow_up);

                        // inner onClick listener to dismiss the expandable by clicking on the expandable itself
                        faq_answerArray[temp].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SoundPoolManager.getInstance().playSound(5);
                                faq_answerArray[temp].collapse();
                                faq_questionArray[temp].setBackgroundResource(R.drawable.faqbottomlineonlybackground);
                                arrowViewArray[temp].setImageResource(R.drawable.faqarrow_down);
                            }
                        });
                    }
                }
            });
        }
    }
}
