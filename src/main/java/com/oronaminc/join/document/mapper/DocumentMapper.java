package com.oronaminc.join.document.mapper;

import com.oronaminc.join.document.domain.Document;
import com.oronaminc.join.room.domain.Room;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentMapper {

    public static Document toDocument(String objectKey, String fileName, Room room) {
        return Document.builder()
                .room(room)
                .fileUrl(objectKey)
                .fileName(fileName)
                .build();
    }


}
