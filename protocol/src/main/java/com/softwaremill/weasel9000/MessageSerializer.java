package com.softwaremill.weasel9000;

import com.google.protobuf.InvalidProtocolBufferException;

public class MessageSerializer {

    public static byte[] serializeMessage(int roomId, Weasel.VOTE_TYPE voteType) {
        return Weasel.Vote
                .newBuilder()
                .setRoomId(roomId)
                .setVote(voteType)
                .setTimestamp(System.currentTimeMillis())
                .build().toByteArray();
    }

    public static Weasel.Vote readMessage(byte[] bytes) {
        try {
            return Weasel.Vote.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            // should never happen. if it does, contact jacek@softwaremill.com
            throw new RuntimeException(e);
        }
    }
}
