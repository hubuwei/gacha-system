package com.cheng.mall.dto;

import lombok.Data;

@Data
public class GameIntroResponse {
    private String title;
    private String summary;
    private String background;
    private String gameplay;
    private String highlights;
    private String recommendation;
}
