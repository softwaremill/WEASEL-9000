package com.softwaremill.weasel9000

import com.softwaremill.weasel9000.MessageSerializer
import com.softwaremill.weasel9000.VertxBuses
import com.softwaremill.weasel9000.Weasel
import org.mapdb.DB
import org.mapdb.DBMaker
import org.vertx.java.core.Handler
import org.vertx.java.core.eventbus.Message
import org.vertx.java.platform.Verticle

class ServerVerticle extends Verticle {

    void start() {

        final DB db = DBMaker.newFileDB(new File("/tmp/weasel.db"))
                .encryptionEnable("this is sparta!")
                .closeOnJvmShutdown()
                .make();

        vertx.eventBus().registerHandler(VertxBuses.BUTTON_BUS, new Handler<Message>() {
            @Override
            public void handle(Message message) {
                Weasel.Vote vote = MessageSerializer.readMessage((byte[]) message.body());
                System.out.println("Button state is: " + vote);

                def votesPerRoom = db.getTreeMap(String.valueOf(vote.getRoomId()));

                votesPerRoom.put(UUID.randomUUID().toString(), vote);

                db.commit();

                println("Votes for room " + vote.getRoomId())

                votesPerRoom.each {println it}
            }
        });

        System.out.println("Server started");
    }
}