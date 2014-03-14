package com.softwaremill.weasel9000

import groovy.transform.TypeChecked
import org.mapdb.DB
import org.mapdb.DBMaker
import org.vertx.java.core.eventbus.Message
import org.vertx.java.platform.Verticle

@TypeChecked
class ServerVerticle extends Verticle {

    void start() {

        final DB db = DBMaker.newFileDB(new File("/tmp/weasel.db"))
                .encryptionEnable("this is sparta!")
                .closeOnJvmShutdown()
                .make()

        vertx.eventBus().registerHandler(VertxBuses.BUTTON_BUS) {
            Message message ->
                Weasel.Vote vote = MessageSerializer.readMessage((byte[]) message.body())
                println("Button state is: " + vote)

                def votesPerRoom = db.getTreeMap(String.valueOf(vote.getRoomId()))

                votesPerRoom.put(UUID.randomUUID().toString(), vote)

                db.commit()

                println("Votes for room " + vote.getRoomId())

                votesPerRoom.each { println it }
        }

        println("Server started")
    }
}