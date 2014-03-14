package com.softwaremill.weasel9000

import groovy.transform.TypeChecked
import org.mapdb.DB
import org.mapdb.DBMaker
import org.vertx.java.core.eventbus.Message
import org.vertx.java.platform.Verticle
import org.vertx.java.core.Handler;

@TypeChecked
class ServerVerticle extends Verticle {

    void start() {

        final DB db = DBMaker.newFileDB(new File("/tmp/weasel.db"))
                .encryptionEnable("this is sparta!")
                .closeOnJvmShutdown()
                .make()

        vertx.eventBus().registerHandler(VertxBuses.BUTTON_BUS, new Handler<Message>(){
            @Override
            void handle(Message message) {
                Weasel.Vote vote = MessageSerializer.readMessage((byte[]) message.body())

                def votesPerRoom = db.getTreeMap(String.valueOf(vote.getRoomId()))

                votesPerRoom.put(UUID.randomUUID().toString(), vote)

                db.commit()

                println "New vote: $vote"
            }
        })

        println("Server started")
    }
}