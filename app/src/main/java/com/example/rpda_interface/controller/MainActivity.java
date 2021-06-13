package com.example.rpda_interface.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpda_interface.R;
import com.example.rpda_interface.custom_listener.ConnectionFailedListener;
import com.example.rpda_interface.custom_listener.CurrentStateChangedListener;
import com.example.rpda_interface.custom_listener.DataReadyListener;
import com.example.rpda_interface.custom_listener.LinkInsertedListener;
import com.example.rpda_interface.custom_listener.RpdaStackChangedListener;
import com.example.rpda_interface.model.ActionKind;
import com.example.rpda_interface.model.automaton.RpdaSet;
import com.example.rpda_interface.model.automaton.VisualRpdaStack;
import com.example.rpda_interface.networking.RSABaseRepository;
import com.example.rpda_interface.viewmodel.RPDAViewModel;

import java.util.ArrayList;


public class MainActivity extends Activity implements PopupMenu.OnMenuItemClickListener,
        DataReadyListener, ConnectionFailedListener,
        LinkInsertedListener, CurrentStateChangedListener,
        RpdaStackChangedListener {

    private LinearLayout automatonLayout;
    private EditText linkStateText;
    private RPDAViewModel rpdaViewModel;
    private AutomatonCanvas automatonCanvas;
    private RSABaseRepository rsaBaseRepo;
    private RpdaSet rpdaSet;
    private Handler uiHandler;
    private ActionKind setAction;
    private ArrayList<Integer> selectedItems;
    private ArrayList<Integer> selectedCriterion;
    private ArrayList<Integer> selectedObjects;
    private RecyclerView rpdaStack;
    private VisualRpdaStack stack;
    private StackRecyclerAdapter adapter;
    private final String[] choices = new String[]{"only perceived object",
                                                  "only topmost stack-symbol",
                                                  "take both into account"};
    private final String[] criteria = new String[]{"pose", "subtask", "tee_dunkel", "tee_gelb"};
    private final String[]  objectNames = new String[]{"tee_dunkel", "tee_gelb"};
    private boolean inExecution = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rpdaStack = findViewById(R.id.stack_recycler_view);
        stack = new VisualRpdaStack();
        adapter = new StackRecyclerAdapter(stack, this);
        rpdaStack.setAdapter(adapter);
        rpdaStack.setLayoutManager(new LinearLayoutManager(this));

        rpdaViewModel = new RPDAViewModel(this);
        automatonCanvas = new AutomatonCanvas(this, rpdaViewModel);
        automatonCanvas.setLinkInsertedListener(this);
        automatonCanvas.setCurrentStateChangedListener(this);
        rsaBaseRepo = new RSABaseRepository(rpdaViewModel);
        rsaBaseRepo.setRpdaStack(stack);
        rsaBaseRepo.setStackAdapter(adapter);
        rsaBaseRepo.setRpdaStackChangedListener(this);
        rsaBaseRepo.setSetDataReadyListener(this);
        rsaBaseRepo.setDataReadyListener((DataReadyListener) () -> {
            if (automatonCanvas != null) {
                automatonCanvas.updateRpda();
                if (inExecution) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    rsaBaseRepo.sendActionInfo(ActionKind.EXECUTE);
                }
            }
        });
        rsaBaseRepo.setConnectionFailedListener(this);
        Thread concurrentActionListener = new Thread(rsaBaseRepo);
        concurrentActionListener.start();

        automatonLayout = findViewById(R.id.automatonLayout);
        linkStateText = findViewById(R.id.linkStateInput);

        //HorizontalScrollView is customized to allow 2d scrolling
        //and to allow child-controls to access relevant touch- and gesture-events
        HorizontalScrollView horizontalScroller = new HorizontalScrollView(this) {
            View content;

            @Override
            public void addView(View view){
                super.addView(view);
                content = view;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                boolean  f = content.onTouchEvent(event);
                super.onTouchEvent(event);
                return true;
            }
        };

        //ScrollView is customized to allow 2d scrolling
        //and to allow child-controls to access relevant touch- and gesture-events
        ScrollView verticalScroller = new ScrollView(this) {
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
        automatonLayout.addView(verticalScroller);


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

        selectedItems = new ArrayList<>();
        selectedCriterion = new ArrayList<>();
        selectedItems = new ArrayList<>();
        automatonCanvas.updateRpda();
        uiHandler = new Handler(Looper.getMainLooper());
    }


    public void showActionMenu(View v) {
        showMenu(v, R.menu.action_menu);
    }

    public void showPushActionMenu(View v) {
        showMenu(v, R.menu.push_action_menu);
    }

    public void showMoveActionMenu(View v) {
        showMenu(v, R.menu.move_action_menu);
    }

    private void showMenu(View v, int id) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(id);
        popup.show();
    }


    private void showBranchingCriteriaDialog() {
        selectedItems.clear();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("on which criteria should the new branch be chosen?");
        builder.setMultiChoiceItems(choices, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            selectedItems.add(which);
                            for (int i = 0; i < choices.length; i++) {
                                if (i != which)
                                    onClick(dialog, i, false);
                            }
                        } else if (selectedItems.contains(which)) {
                            selectedItems.remove(Integer.valueOf(which));
                            ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                        }
                    }
                });
        builder.setPositiveButton("ok", (dialog, id) -> onBranchingCriteriaSelected());
        builder.setNegativeButton("cancel", (dialog, id) -> {});
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showPushObjectDialog() {
        selectedObjects.clear();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("select object to push");
        builder.setMultiChoiceItems(objectNames, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            selectedObjects.add(which);
                            for (int i = 0; i < objectNames.length; i++) {
                                if (i != which)
                                    onClick(dialog, i, false);
                            }
                        } else if (selectedObjects.contains(which)) {
                            selectedObjects.remove(Integer.valueOf(which));
                            ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                        }
                    }
                });
        builder.setPositiveButton("ok", (dialog, id) -> onObjectSelected());
        builder.setNegativeButton("cancel", (dialog, id) -> {});
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.push_pose:
                    rsaBaseRepo.sendActionInfo(ActionKind.PUSH_POSE);
                    return true;
                case R.id.push_object_id:
                    showPushObjectDialog();
                    return true;
                case R.id.scan_object:
                    showBranchingCriteriaDialog();
                    return true;
                case R.id.gripper_action:
                    rsaBaseRepo.sendActionInfo(ActionKind.GRIPPER);
                    return true;
                case R.id.push_subtask:
                    rsaBaseRepo.sendActionInfo(ActionKind.REQUEST_SET_INFO);
                    setAction = ActionKind.PUSH_SUBTASK;
                    return true;
                case R.id.create_subtask:
                    rsaBaseRepo.sendActionInfo(ActionKind.CREATE_SUBTASK, linkStateText.getText().toString());
                    return true;
                case R.id.switch_subtask:
                    rsaBaseRepo.sendActionInfo(ActionKind.REQUEST_SET_INFO);
                    setAction = ActionKind.SWITCH_SUBTASK;
                    return true;
                case R.id.push_absolute_pose:
                    rsaBaseRepo.sendActionInfo(ActionKind.PUSH_ABSOLUTE_POSE);
                    return true;
                case R.id.reset_subtask:
                    rsaBaseRepo.sendActionInfo(ActionKind.RESET_SUBTASK);
                    return true;
                case R.id.restart_program:
                    rsaBaseRepo.sendActionInfo(ActionKind.RESTART);
                    return true;
                case R.id.move_abs:
                    rsaBaseRepo.sendActionInfo(ActionKind.MOVE_ABS);
                    return true;
                case R.id.move_relative:
                    rsaBaseRepo.sendActionInfo(ActionKind.MOVE_REL);
                    return true;
                case R.id.move_rel_obj:
                    rsaBaseRepo.sendActionInfo(ActionKind.MOVE_REL_OBJ);
                    return true;
                case R.id.branch:
                    rsaBaseRepo.sendActionInfo(ActionKind.BRANCH);
                    return true;
                case R.id.link_state:
                    automatonCanvas.linkByTap = 0;
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String selectedRpdaName = data.getStringExtra("resName");
        ActionKind actionKind = ActionKind.valueOf(data.getStringExtra("action_name"));
        if (actionKind == ActionKind.SWITCH_SUBTASK) {
            rsaBaseRepo.sendActionInfo(actionKind, selectedRpdaName);
        } else if (actionKind == ActionKind.PUSH_SUBTASK) {
            rsaBaseRepo.sendActionInfo(actionKind, selectedRpdaName);
        }
    }


    @Override
    public void onDataReady() {
        rpdaSet = rsaBaseRepo.getRpdaSet();
        Intent intent = new Intent(this, SubtaskSelectorActivity.class);
        intent.putExtra("action_name", setAction.name());
        intent.putExtra("rpdaSet", rpdaSet);
        startActivityForResult(intent, 0);
    }


    @Override
    public void onConnectionFailed() {
        Looper.prepare();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(this, "Connection Failed. Please check Network. Reconnecting...", duration);
        toast.show();

        //TODO reconnection attempt via dialog or timer
    }


    @Override
    public void onLinkInserted(int targetId) {
        rsaBaseRepo.sendActionInfo(ActionKind.INSERT_LINK, Integer.toString(targetId));
    }


    private void onBranchingCriteriaSelected() {
        if (selectedItems.contains(0)) {
            rsaBaseRepo.sendActionInfo(ActionKind.SCAN_FOR_OBJ);
            return;
        }
        String specifier = "";
        if (selectedItems.contains(1)) {
            specifier += "S";
        } else if (selectedItems.contains(2))
            specifier += "B";

        selectedCriterion.clear();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("determine the kind of the stack-criterion:");
        builder.setMultiChoiceItems(criteria, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            selectedCriterion.add(which);
                            for (int i = 0; i < criteria.length; i++) {
                                if (i != which)
                                    onClick(dialog, i, false);
                            }
                        } else if (selectedCriterion.contains(which)) {
                            selectedCriterion.remove(Integer.valueOf(which));
                            ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                        }
                    }
                });
        String finalSpecifier = specifier;  //use in lambda expression forces final parameters
        builder.setPositiveButton("ok", (dialog, id) -> onBranchingCriterionSelected(finalSpecifier));
        builder.setNegativeButton("cancel", (dialog, id) -> {});
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void onBranchingCriterionSelected(String specifier) {
        //selectedCriterion holds only one value; this value serves as index for the selected string in criteria
        String criterion = criteria[selectedCriterion.get(0)];
        if (specifier.equals("S")) {
            rsaBaseRepo.sendActionInfo(ActionKind.BRANCH_TOPMOST_SYMBOL, criterion);
        } else if (specifier.equals("B")) {
            rsaBaseRepo.sendActionInfo(ActionKind.BRANCH_STIMULUS_AND_TOPMOST, criterion);
        }
    }


    private void onObjectSelected() {
        //selectedObjects can only hold one value that serves as index for the selected string in objectNames
        String object = objectNames[selectedObjects.get(0)];
        rsaBaseRepo.sendActionInfo(ActionKind.PUSH_OBJ_ID, object);
    }


    public void showEditMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.edit_action_menu);
        popup.show();
    }


    public void popAction(View view) {
        rsaBaseRepo.sendActionInfo(ActionKind.POP);
    }


    @Override
    public void onCurrentStateChanged(int id) {
        rsaBaseRepo.sendActionInfo(ActionKind.CSTATE_CHANGED, String.valueOf(id));
    }


    @Override
    /*
     * inform the main-thread looper that a stack-recyclerview update is pending
     */
    public void onRpdaStackChanged() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }


    public void play(View view) {
        inExecution = true;
        setButtonsEnabledConfig(true, true, false, false);
        rsaBaseRepo.sendActionInfo(ActionKind.JUMP);
    }


    public void step(View view) {
        if (!inExecution) {
            rsaBaseRepo.sendActionInfo(ActionKind.EXECUTE);
        }
    }

    public void pause(View view) {
        inExecution = false;
        setButtonsEnabledConfig(true, true, true, false);
    }

    public void stop(View view) {
        inExecution = false;
        setButtonsEnabledConfig(true, true, true, true);
        rsaBaseRepo.sendActionInfo(ActionKind.STOP);
    }

    /**
     * enable or disable pause, stop, step and play buttons
     * @param pause true if pause button shall be enabled, false otherwise
     * @param stop true if stop button shall be enabled, false otherwise
     * @param step true if step button shall be enabled, false otherwise
     * @param play true if play button shall be enabled, false otherwise
     */
    private void setButtonsEnabledConfig(boolean pause, boolean stop, boolean step, boolean play) {
        findViewById(R.id.pause_button).setEnabled(pause);
        findViewById(R.id.stop_button).setEnabled(stop);
        findViewById(R.id.step_button).setEnabled(step);
        findViewById(R.id.play_button).setEnabled(play);
    }
}