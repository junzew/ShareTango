package com.imran.wali.sharetango.DataRepository;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by wali on 14/03/17.
 */
@JsonObject
public class Message {
    @JsonField
    public String lol;
}
