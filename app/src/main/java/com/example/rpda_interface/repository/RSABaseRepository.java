package com.example.rpda_interface.repository;

import com.example.rpda_interface.DataReadyListener;
import com.example.rpda_interface.model.automaton.RpdaSet;
import com.example.rpda_interface.model.socketConnector.SocketConnector;
import com.example.rpda_interface.model.action.ActionKind;
import com.example.rpda_interface.model.automaton.VisualRPDA;
import com.example.rpda_interface.viewmodel.RPDAViewModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;

public class RSABaseRepository implements Runnable {

    private static ActionKind currentActionKind = ActionKind.NO_ACTION;
    private RPDAViewModel rpdaViewModel;
    public RpdaSet rpdaSet;
    private DataReadyListener dataReadyListener;
    private DataReadyListener setDataReadyListener;


    public RSABaseRepository(RPDAViewModel rpdaViewModel) {
        this.rpdaViewModel = rpdaViewModel;
    }

    public void setDataReadyListener(DataReadyListener dataReadyListener) {
        this.dataReadyListener = dataReadyListener;
    }

    public void setSetDataReadyListener(DataReadyListener setDataReadyListener) {
        this.setDataReadyListener = setDataReadyListener;
    }

    @Override
    public void run() {
        try {
            SocketConnector.initializeSocket();
        } catch (IOException e) {
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
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("RSABaseRepository-Thread " + Thread.currentThread().getId() + " will be shut down due to error.");
        } catch (NullPointerException e) {
            System.err.println("Error in InputStream / Reader!");
        }
    }


    private synchronized void setCurrentActionKind(ActionKind actionKind) {
        currentActionKind = actionKind;
    }


    private void updateRpdaSet(String message) {
        //rpdaViewModel.generateNewRpda();
        String name = "";
        CharacterIterator iterator = new StringCharacterIterator(message);
        iterator.next();
        while (iterator.current() != ',') {
            name+=iterator.current();
            iterator.next();
        }

        rpdaViewModel.generateNewRpda(name);
        iterator.next();
        //rpda = new VisualRPDA(name);
        HashMap<Integer, Integer> transitions = new HashMap<>();

        while(iterator.current() != CharacterIterator.DONE) {
                String id = "";
                while (iterator.current() != ',') {
                    id += iterator.current();
                    iterator.next();
                }

                iterator.next();

                while (iterator.current() != ';') {
                    boolean sem = false;
                    String targetId="";
                    while (iterator.current() != ',') {
                        if (iterator.current() == ';') {
                            sem=true;
                            break;
                        }
                        targetId += iterator.current();
                        iterator.next();
                    }

                    transitions.put(Integer.parseInt(id), Integer.parseInt(targetId));
                    if (!sem)
                      iterator.next();
                }

                rpdaViewModel.handleStateAction(Integer.parseInt(id));
                iterator.next();
        }

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


    /**
     * Notify the main-system about an Action that was triggered by the user
     * @param actionKind the ActionKind that originated from the rpda_interface
     */
    public void sendActionInfo(ActionKind actionKind) {
        try {
            ActionKind formerActionKind = currentActionKind;
            Runnable task = () -> {
                setCurrentActionKind(actionKind);
                try {
                    PrintWriter transmitter = SocketConnector.getTransmitter();
                    transmitter.write(actionKind.name());
                    transmitter.flush();
                } catch (IOException e) {
                    e.printStackTrace(); //TODO handle connection loss -> try reconnect or the like
                    currentActionKind = formerActionKind;
                    System.err.println("Invariant violated, try resetting connection");
                }
            };
            new Thread(task).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify the main-system about an Action that was triggered by the user
     * @param actionKind the ActionKind that originated from the rpda_interface
     */
    public void sendActionInfo(ActionKind actionKind, String additionalInfo) {
        try {
            ActionKind formerActionKind = currentActionKind;
            Runnable task = () -> {
                setCurrentActionKind(actionKind);
                try {
                    PrintWriter transmitter = SocketConnector.getTransmitter();
                    transmitter.write(actionKind.name() + "," + additionalInfo + ";");
                    transmitter.flush();
                } catch (IOException e) {
                    e.printStackTrace(); //TODO handle connection loss -> try reconnect or the like
                    currentActionKind = formerActionKind;
                    System.err.println("Invariant violated, try resetting connection");
                }
            };
            new Thread(task).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public RpdaSet getRpdaSet() {
        return rpdaSet;
    }
}
