package com.tonkar.volleyballreferee.api;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter @EqualsAndHashCode
public class ApiSanction {

    @SerializedName("card")
    private SanctionType card;
    @SerializedName("num")
    private int          num;
    @SerializedName("set")
    private int          set;
    @SerializedName("homePoints")
    private int          homePoints;
    @SerializedName("guestPoints")
    private int          guestPoints;

}


