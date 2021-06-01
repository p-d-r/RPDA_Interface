package com.example.rpda_interface.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.rpda_interface.ConnectionFailedListener;
import com.example.rpda_interface.DataReadyListener;
import com.example.rpda_interface.LinkInsertedListener;
import com.example.rpda_interface.R;
import com.example.rpda_interface.model.action.ActionKind;
import com.example.rpda_interface.model.automaton.RpdaSet;
import com.example.rpda_interface.model.socketConnector.SocketConnector;
import com.example.rpda_interface.repository.RSABaseRepository;
import com.example.rpda_interface.viewmodel.RPDAViewModel;

import java.util.ArrayList;


public class MainActivity extends Activity implements PopupMenu.OnMenuItemClickListener,
                                           DataReadyListener, ConnectionFailedListener,
                                           LinkInsertedListener {

    private LinearLayout containerLayout;
    private EditText linkStateText;
    private RPDAViewModel rpdaViewModel;
    private AutomatonCanvas automatonCanvas;
    private HorizontalScrollView horizontalScroller;
    private ScrollView verticalScroller;
    private RSABaseRepository rsaBaseRepo;
    private RpdaSet rpdaSet;
    private ActionKind setAction;
    private ArrayList<Integer> selectedItems;
    private final String[] choices = new String[]{"only perceived object",
                                                  "only topmost stack-symbol",
                                                  "take both into account", "Object1 (tee gelb)",
                                                  "Object2 (tee dunkel) ", "Object3 (salz)"};
    static int ids = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rpdaViewModel = new RPDAViewModel(this);
        automatonCanvas = new AutomatonCanvas(this, rpdaViewModel);
        automatonCanvas.setLinkInsertedListener(this);
        rsaBaseRepo = new RSABaseRepository(rpdaViewModel);
        rsaBaseRepo.setSetDataReadyListener(this);
        rsaBaseRepo.setDataReadyListener(new DataReadyListener() {
            @Override
            public void onDataReady() {
                if (automatonCanvas != null) {
                    automatonCanvas.updateRpda();
                }
            }
        });
        rsaBaseRepo.setConnectionFailedListener(this);

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
                super.onTouchEvent(event);
                return true;
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

    selectedItems = new ArrayList<>();
        //rpdaViewModel.generateTestRpda();
        //automatonCanvas.updateRpda();
    }


    public void createState(View view) {
        automatonCanvas.updateRpda();
        automatonCanvas.invalidate();
    }


    public boolean execute(View v) {
        rsaBaseRepo.sendActionInfo(ActionKind.EXECUTE);
        return true;
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


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.push_pose:
                    rsaBaseRepo.sendActionInfo(ActionKind.PUSH_POSE);
                    return true;
                case R.id.push_object_id:
                    rsaBaseRepo.sendActionInfo(ActionKind.PUSH_OBJ_ID);
                    return true;
                case R.id.scan_object:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("on which criteria should the new branch be chosen?");
                    builder.setMultiChoiceItems(choices, null,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which,
                                                    boolean isChecked) {
                                    if (isChecked) {
                                        // If the user checked the item, add it to the selected items
                                        selectedItems.add(which);
                                    } else if (selectedItems.contains(which)) {
                                        // Else, if the item is already in the array, remove it
                                        selectedItems.remove(Integer.valueOf(which));
                                    }
                                }
                            });
                    builder.setPositiveButton("ok", (dialog, id)
                            -> onBranchingCriteriaSelected());
                    builder.setNegativeButton("cancel", (dialog, id) -> {});
                    AlertDialog dialog = builder.create();
                    dialog.show();
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
                case R.id.zoom_restore:
                    automatonCanvas.restoreDefaultZoom();
                    return true;
                case R.id.pop_pose:
                    rsaBaseRepo.sendActionInfo(ActionKind.POP);
                    return true;
                case R.id.pop_subtask:
                    rsaBaseRepo.sendActionInfo(ActionKind.POP);
                    return true;
                case R.id.pop_object_id:
                    rsaBaseRepo.sendActionInfo(ActionKind.POP);
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


    public void showPushActionMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.push_action_menu);
        popup.show();
    }


    public void showPopActionMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.pop_action_menu);
        popup.show();
    }


    public void showMoveActionMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(MainActivity.this);
        popup.inflate(R.menu.move_action_menu);
        popup.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case 0:
                String selectedRpdaName = data.getStringExtra("resName");
                ActionKind actionKind = ActionKind.valueOf(data.getStringExtra("action_name"));
                if (actionKind == ActionKind.SWITCH_SUBTASK) {
                    rsaBaseRepo.sendActionInfo(actionKind, selectedRpdaName);
                } else if (actionKind == ActionKind.PUSH_SUBTASK) {
                    rsaBaseRepo.sendActionInfo(actionKind, selectedRpdaName);
                } break;
            case 1:
                String ip = data.getStringExtra("target_ip");
                SocketConnector.dyn_ip_ipv4=ip; break;
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

    public void showOptionsActivity(View view) {
        Intent intent = new Intent(this, OptionsActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onConnectionFailed() {
        Looper.prepare();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, "Connection Failed. Please check Network. Reconnecting...", duration);
        toast.show();
/*
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            return;
        }
        rsaBaseRepo = new RSABaseRepository(rpdaViewModel);
        rsaBaseRepo.setSetDataReadyListener(this);
        rsaBaseRepo.setDataReadyListener(new DataReadyListener() {
            @Override
            public void onDataReady() {
                if (automatonCanvas != null) {
                    automatonCanvas.updateRpda();
                }
            }
        });

        rsaBaseRepo.setConnectionFailedListener(this);

        Thread concurrentActionListener = new Thread(rsaBaseRepo);
        concurrentActionListener.start();*/
    }

    public void setLinkByTap(View view) {
        automatonCanvas.linkByTap = 0;
    }

    @Override
    public void onLinkInserted(int targetId) {
        rsaBaseRepo.sendActionInfo(ActionKind.INSERT_LINK, Integer.toString(targetId));
    }


    public void onBranchingCriteriaSelected() {
        if (selectedItems.contains(0)) {
            rsaBaseRepo.sendActionInfo(ActionKind.SCAN_FOR_OBJ);
            return;
        }
        String specifier = "";
        if (selectedItems.contains(1)) {
            specifier += "s";
        } else if (selectedItems.contains(2))
            specifier += "b";
            if (selectedItems.contains(3))
                specifier += "3";
            else if (selectedItems.contains(4))
                specifier += "4";
            else if(selectedItems.contains(5))
                specifier += "5";

        rsaBaseRepo.sendActionInfo(ActionKind.SCAN_FOR_OBJ, specifier);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("do you want to push the perceived object?");
        builder.setPositiveButton("yes", (newDialog, id)
                -> rsaBaseRepo.sendActionInfo(ActionKind.PUSH_OBJ_ID));
        builder.setNegativeButton("No", (newDialog, which) -> {
            return;
        });
        AlertDialog newDialog = builder.create();
        newDialog.show();
    }
}