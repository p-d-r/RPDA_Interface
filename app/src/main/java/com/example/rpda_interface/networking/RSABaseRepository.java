package com.example.rpda_interface.networking;

import com.example.rpda_interface.custom_listener.ConnectionFailedListener;
import com.example.rpda_interface.custom_listener.DataReadyListener;
import com.example.rpda_interface.custom_listener.RpdaStackChangedListener;
import com.example.rpda_interface.controller.StackRecyclerAdapter;
import com.example.rpda_interface.model.automaton.RpdaSet;
import com.example.rpda_interface.model.automaton.VisualRpdaStack;
import com.example.rpda_interface.model.ActionKind;
import com.example.rpda_interface.viewmodel.RPDAViewModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

public class RSABaseRepository implements Runnable {

    private static ActionKind currentActionKind = ActionKind.NO_ACTION;
    private RPDAViewModel rpdaViewModel;
    public RpdaSet rpdaSet;
    public VisualRpdaStack rpdaStack;
    private StackRecyclerAdapter adapter;
    private DataReadyListener dataReadyListener;
    private DataReadyListener setDataReadyListener;
    private ConnectionFailedListener connectionFailedListener;
    private RpdaStackChangedListener rpdaStackChangedListener;


    public RSABaseRepository(RPDAViewModel rpdaViewModel) {
        this.rpdaViewModel = rpdaViewModel;
    }

    public void setDataReadyListener(DataReadyListener dataReadyListener) {
        this.dataReadyListener = dataReadyListener;
    }

    public void setSetDataReadyListener(DataReadyListener setDataReadyListener) {
        this.setDataReadyListener = setDataReadyListener;
    }

    public void setConnectionFailedListener(ConnectionFailedListener connectionFailedListener) {
        this.connectionFailedListener = connectionFailedListener;
    }

    public void setRpdaStackChangedListener(RpdaStackChangedListener rpdaStackChangedListener) {
        this.rpdaStackChangedListener = rpdaStackChangedListener;
    }

    @Override
    public void run() {
        try {
            SocketConnector.initializeSocket();
        } catch (IOException e) {
            connectionFailedListener.onConnectionFailed();
            return;
        }
        InputStreamReader receiver;

        try {
            receiver = SocketConnector.getReceiver();
            BufferedReader reader = new BufferedReader(receiver);

            while (currentActionKind != ActionKind.QUIT) {
                String str = reader.readLine();
                System.out.println("Message: " + str);
                switch(str.charAt(0)) {
                    case '0': updateRpdaSet(str); break;
                    case '1': getRpdaSetInfo(str); break;
                    case '2': getStackInfo(str); break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("RSABaseRepository-Thread " + Thread.currentThread().getId() + " will be shut down due to error.");
            connectionFailedListener.onConnectionFailed();
        } catch (NullPointerException e) {
            e.printStackTrace();
            connectionFailedListener.onConnectionFailed();
        }
    }


    private void updateRpdaSet(String message) {
        String name = "";
        CharacterIterator iterator = new StringCharacterIterator(message);
        iterator.next();
        while (iterator.current() != ',') {
            name+=iterator.current();
            iterator.next();
        }

        rpdaViewModel.generateNewRpda(name);
        iterator.next();
        ArrayList<Integer> origins = new ArrayList<>();
        ArrayList<Integer> targets = new ArrayList<>();
        ArrayList<String> actions = new ArrayList<>();
        ArrayList<String> transitionCriteria = new ArrayList<>();
        ArrayList<String> transitionSamples = new ArrayList<>();
        boolean currentState = false;
        boolean isBranching = false;

        while(iterator.current() != CharacterIterator.DONE) {
                String id = "";
                while (iterator.current() != ',') {
                    if (iterator.current() == 'C') {
                        currentState = true;
                        iterator.next();
                    }
                    if (iterator.current() == 'B'){
                        isBranching = true;
                        iterator.next();
                    }
                    id += iterator.current();
                    iterator.next();
                }

                while (true) {
                    String targetId=getNextBlock(iterator);
                    if (targetId.equals(""))
                        break;

                    String actionKind = getNextBlock(iterator);
                    String branchingSample = getNextBlock(iterator);
                    String branchingSymbol = getNextBlock(iterator);
                    origins.add(Integer.parseInt(id));
                    targets.add(Integer.parseInt(targetId));
                    actions.add(actionKind);
                    transitionCriteria.add(branchingSymbol);
                    transitionSamples.add(branchingSample);
                }

                rpdaViewModel.handleStateAction(Integer.parseInt(id));
                if (currentState)
                    rpdaViewModel.setCurrent(Integer.parseInt(id));
                if (isBranching)
                    rpdaViewModel.setBranchingState(Integer.parseInt(id));

                currentState = false;
                isBranching = false;
                iterator.next();
        }

        rpdaViewModel.generateTransitions(origins, targets, actions, transitionCriteria, transitionSamples);
        dataReadyListener.onDataReady();
    }


    private void getRpdaSetInfo(String message) {
        rpdaSet  = new RpdaSet();
        CharacterIterator iterator = new StringCharacterIterator(message);
        while(iterator.current() != iterator.DONE) {
            String name = "";
            String id = "";
            while (iterator.next() != ',') {
                if (iterator.current() == iterator.DONE) {
                    setDataReadyListener.onDataReady();
                    return;
                }

                id += iterator.current();
            }

            while (iterator.next() != ';') {
                if (iterator.current() == iterator.DONE) {
                    setDataReadyListener.onDataReady();
                    return;
                }

                name += iterator.current();
            }

            rpdaSet.addRpda(name);
        }

        setDataReadyListener.onDataReady();
    }


    private void getStackInfo(String message) {
        CharacterIterator iterator = new StringCharacterIterator(message);
        rpdaStack.clear();
        while (iterator.current() != CharacterIterator.DONE && iterator.current() != ';') {
            String item = getNextBlock(iterator);
            System.out.println(item);
            rpdaStack.addStackItem(item);
        }

        rpdaStackChangedListener.onRpdaStackChanged();
    }


    public void setRpdaStack(VisualRpdaStack rpdaStack) {
        this.rpdaStack = rpdaStack;
    }

    public void setStackAdapter(StackRecyclerAdapter adapter) {
        this.adapter = adapter;
    }


    /**
     * Notify the main-system about an Action that was triggered by the user
     * @param actionKind the ActionKind that originated from the rpda_interface
     */
    public void sendActionInfo(ActionKind actionKind) {
        try {
            Runnable task = () -> {
                try {
                    PrintWriter transmitter = SocketConnector.getTransmitter();
                    transmitter.write(actionKind.name());
                    transmitter.flush();
                } catch (IOException e) {
                    e.printStackTrace(); //TODO handle connection loss -> try reconnect or the like
                    connectionFailedListener.onConnectionFailed();
                }
            };
            new Thread(task).start();
        } catch (Exception e) {
            e.printStackTrace();
            connectionFailedListener.onConnectionFailed();
        }
    }

    /**
     * Notify the main-system about an Action that was triggered by the user
     * @param actionKind the ActionKind that originated from the rpda_interface
     */
    public void sendActionInfo(ActionKind actionKind, String additionalInfo) {
        try {
            Runnable task = () -> {
                try {
                    PrintWriter transmitter = SocketConnector.getTransmitter();
                    transmitter.write(actionKind.name() + "," + additionalInfo + ";");
                    transmitter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    connectionFailedListener.onConnectionFailed();
                }
            };
            new Thread(task).start();
        } catch (Exception e) {
            e.printStackTrace();
            connectionFailedListener.onConnectionFailed();
        }
    }


    private String getNextBlock(CharacterIterator iterator) {
        String block = "";
        if (iterator.current() == ';')
            return block;
        while (iterator.next() != CharacterIterator.DONE && iterator.current() != ';' && iterator.current() != ',') {
            block += iterator.current();
        }

        return block;
    }

    public RpdaSet getRpdaSet() {
        return rpdaSet;
    }
}
