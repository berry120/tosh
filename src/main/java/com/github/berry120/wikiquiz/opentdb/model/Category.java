package com.github.berry120.wikiquiz.opentdb.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Category {

    GENERAL_KNOWLEDGE("9"),
    ENTERTAINMENT_BOOKS("10"),
    ENTERTAINMENT_FILM("11"),
    ENTERTAINMENT_MUSIC("12"),
    ENTERTAINMENT_MUSICALS_THEATRES("13"),
    ENTERTAINMENT_TV("14"),
    ENTERTAINMENT_VIDEO_GAMES("15"),
    ENTERTAINMENT_BOARD_GAMES("16"),
    SCIENCE_NATURE("17"),
    SCIENCE_COMPUTERS("18"),
    SCIENCE_MATHEMATICS("19"),
    MYTHOLOGY("20"),
    SPORTS("21"),
    GEOGRAPHY("22"),
    HISTORY("23"),
    POLITICS("24"),
    ART("25"),
    CELEBRITIES("26"),
    ANIMALS("27"),
    VEHICLES("28"),
    ENTERTAINMENT_COMICS("29"),
    SCIENCE_GADGETS("30"),
    ENTERTAINMENT_ANIME_MANGA("31"),
    ENTERTAINMENT_CARTOONS_ANIMATIONS("32");

    private final String category;

    public String toParamValue() {
        return category;
    }

}