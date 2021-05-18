package com.example.rpda_interface.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.rpda_interface.R;
import com.example.rpda_interface.model.action.ActionKind;
import com.example.rpda_interface.model.automaton.RpdaSet;
import com.example.rpda_interface.repository.RSABaseRepository;
import com.example.rpda_interface.viewmodel.RPDAViewModel;


public class MainActivity extends Activity implements PopupMenu.OnMenuItemClickListener
{

    LinearLayout containerLayout;
    EditText linkStateText;
    RPDAViewModel rpdaViewModel;
    AutomatonCanvas automatonCanvas;
    HorizontalScrollView horizontalScroller;
    ScrollView verticalScroller;
    RSABaseRepository rsaBaseRepo;
    static int ids = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rpdaViewModel = new RPDAViewModel(this);
        automatonCanvas = new AutomatonCanvas(this, rpdaViewModel);
        rsaBaseRepo = new RSABaseRepository(rpdaViewModel);
        Thread concurrentActionListener = new Thread(rsaBaseRepo);
        concurrentActionListener.start();

        containerLayout = findViewById(R.id.containerLayout);
        linkStateText = findViewById(R.id.linkStateInput);

        //HorizontalScrollView is customized to allow 2d scrolling
        //and to allow child-controls to access relevant touch- and gesture-events
        horizontalScroller = new HorizontalScrollView(this) {
            View content;

            @Override
            public void addView(View view){
                super.addView(view);
                content = view;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                boolean  f = content.onTouchEvent(event);
                return super.onTouchEvent(event);
            }
        };

        //ScrollView is customized to allow 2d scrolling
        //and to allow child-controls to access relevant touch- and gesture-events
        verticalScroller = new ScrollView(this) {
            HorizontalScrollView horizontalScroller;

            @Override
            public void addView(View view){
                super.addView(view);
                if (view instanceof HorizontalScrollView)
                        horizontalScroller = (HorizontalScrollView) view;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                super.onTouchEvent(event);
                horizontalScroller.dispatchTouchEvent(event);
                return true;
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent event)
            {
                super.onInterceptTouchEvent(event);
                horizontalScroller.onInterceptTouchEvent(event);
                return true;
            }
        };

        horizontalScroller.addView(automatonCanvas);
        verticalScroller.addView(horizontalScroller);
        containerLayout.addView(verticalScroller);

        horizontalScroller.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                automatonCanvas.scrollX = horizontalScroller.getScrollX();
            }
        });

        verticalScroller.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                automatonCanvas.scrollY = verticalScroller.getScrollY();
            }
        });
    }



    public void createState(View view) {
        rpdaViewModel.handleStateAction(ids);
        ids++;
        automatonCanvas.invalidate();
    }



    public void linkState(View view) {
        int id;
        String text = linkStateText.getText().toString();
        CharSequence message;

        if (!text.equals("")) {
            id = Integer.parseInt(text);
            if (rpdaViewModel.handleLinkAction(id))
                message = "link inserted!";  //TODO: refactor to resource
            else
                message = "target state id not found!";
        } else {
            message = "given state-identifier is not a number!";
        }

        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, message, duration);
        toast.show();

        automatonCanvas.invalidate();
    }



    public void undoAction(View view) {
        rpdaViewModel.undo();
        automatonCanvas.invalidate();
    }



    public void redoAction(View view) {
        rpdaViewModel.redo();
        automatonCanvas.invalidate();
    }

    public void updateAutomaton(View view) {
        //rpdaViewModel.update();
        automatonCanvas.updateRpda();
    }


    public void showPushActionMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.push_action_menu);
        popup.show();
    }


    public void popStackSymbol(View view) {
        rsaBaseRepo.sendActionInfo(ActionKind.POP);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        try {
            Intent data;
            RpdaSet rpdaSet;
            switch (item.getItemId()) {
                case R.id.push_pose:
                    rsaBaseRepo.sendActionInfo(ActionKind.PUSH_POSE);
                    return true;
                /*case R.id.SHOW_SUBTASKS:

                    return true;*/
                case R.id.push_object_id:
                    rsaBaseRepo.sendActionInfo(ActionKind.PUSH_OBJ_ID);
                    return true;
                case R.id.scan_object:
                    rsaBaseRepo.sendActionInfo(ActionKind.SAMPLE_ADD);
                    return true;
                case R.id.gripper_action:
                    rsaBaseRepo.sendActionInfo(ActionKind.GRIPPER);
                    return true;
                case R.id.push_subtask:
                    rsaBaseRepo.rpdaSet = null;
                    rsaBaseRepo.sendActionInfo(ActionKind.REQUEST_SET_INFO);
                    data = new Intent(this, SubtaskSelectorActivity.class);
                    rpdaSet = rsaBaseRepo.nextSet();
                    data.putExtra("action_name", ActionKind.PUSH_SUBTASK.name());
                    data.putExtra("rpdaSet", rpdaSet);
                    startActivityForResult(data, 0);
                    return true;
                case R.id.create_subtask:
                    rsaBaseRepo.sendActionInfo(ActionKind.CREATE_SUBTASK, linkStateText.getText().toString());
                    return true;
                case R.id.switch_subtask:
                    rsaBaseRepo.rpdaSet = null;
                    rsaBaseRepo.sendActionInfo(ActionKind.REQUEST_SET_INFO);
                    data = new Intent(this, SubtaskSelectorActivity.class);
                    rpdaSet = rsaBaseRepo.nextSet();
                    data.putExtra("action_name", ActionKind.SWITCH_SUBTASK.name());
                    data.putExtra("rpdaSet", rpdaSet);
                    startActivityForResult(data, 0);
                    return true;
                case R.id.update_automaton:
                    automatonCanvas.updateRpda();
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void showActionMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.action_menu);
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //int selectedRpdaId = data.getIntExtra("rpda_id", 0);
        //rsaBaseRepo.sendActionInfo(ActionKind.SWITCH_SUBTASK);
        String selectedRpdaName = data.getStringExtra("resName");
        ActionKind actionKind = ActionKind.valueOf(data.getStringExtra("action_name"));
        if (actionKind == ActionKind.SWITCH_SUBTASK) {
            rsaBaseRepo.sendActionInfo(actionKind, selectedRpdaName);
        } else if (actionKind == ActionKind.PUSH_SUBTASK) {
            rsaBaseRepo.sendActionInfo(actionKind, selectedRpdaName);
        }
    }
}