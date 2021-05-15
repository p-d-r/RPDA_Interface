package com.example.rpda_interface.repository;

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
import java.util.Map;

public class RSABaseRepository implements Runnable {

    private static ActionKind currentActionKind = ActionKind.NO_ACTION;
    private VisualRPDA rpda;
    private RPDAViewModel rpdaViewModel;


    public RSABaseRepository(RPDAViewModel rpdaViewModel) {
        this.rpdaViewModel = rpdaViewModel;
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
                updateRpdaSet(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("RSABaseRepository-Thread " + Thread.currentThread().getId() + " will be shut down due to error.");
        } catch (NullPointerException e) {
            System.err.println("Error in InputStream / Reader!");
        }
    }

    public VisualRPDA getRpda() {
        return this.rpda;
    }

    private synchronized void setCurrentActionKind(ActionKind actionKind) {
        currentActionKind = actionKind;
    }


    private void updateRpdaSet(String message) {
        rpdaViewModel.generateNewRpda();
        String name = "";
        CharacterIterator iterator = new StringCharacterIterator(message);
        while (iterator.current() != ',') {
            name+=iterator.current();
            iterator.next();
        }

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
                //rpda.addState(Integer.parseInt(id));
                iterator.next();
        }

        /*for (Map.Entry<Integer, Integer> entry : transitions.entrySet()) {
            rpda.getState(entry.getKey()).addTransition(rpda.getState(entry.getValue()));
        }*/
    }



    private void handleAction(String actionDesc) {
        //Do conversion of messages here
        ActionKind actionKind = getActionKindFromPrefix(actionDesc);
        switch(actionKind) {
            case MOVE: ;
            case MOVE_ABS: break;
            case MOVE_REL: break;
            case MOVE_REL_OBJ: break;
            case BRANCH: break;
            case JUMP: break;
        }
    }


    /**
     * @param actionDesc Line that was communicated by socket-inputstream
     * @return corresponding ActionKind
     */
    private ActionKind getActionKindFromPrefix(String actionDesc) {
        if (actionDesc == null)
            return ActionKind.NO_ACTION;

        String prefix = "";
        for (int i = 0; i < actionDesc.length(); i++) {
            prefix += actionDesc.charAt(i);
            switch(prefix) {
                case "MOVE_ABS": System.out.println("abs move received."); return ActionKind.MOVE_ABS;
                case "MOVE_REL": System.out.println("rel move received."); return ActionKind.MOVE_REL;
                case "MOVE_OBJ_REL": System.out.println("obj rel move received."); return ActionKind.MOVE_REL_OBJ;
                case "BRANCH": System.out.println("branching-action received"); return ActionKind.BRANCH;
                case "JUMP": System.out.println("jump-action received."); return ActionKind.JUMP;
                case "QUIT": System.out.println("quit action received."); return ActionKind.QUIT;
            }
        }

        return ActionKind.NO_ACTION;
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
}
