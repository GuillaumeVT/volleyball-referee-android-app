package com.tonkar.volleyballreferee.api;

import com.google.gson.annotations.SerializedName;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import lombok.*;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter @EqualsAndHashCode
public class ApiSanction {

    @SerializedName("card")
    private SanctionType card;
    @SerializedName("num")
    private int          num; // 0-99 player, 100 coach, 200 team
    @SerializedName("set")
    private int          set;
    @SerializedName("homePoints")
    private int          homePoints;
    @SerializedName("guestPoints")
    private int          guestPoints;

}


