#!/usr/bin/env python3
"""Gera as texturas de GUI (288x206) do Avoid Miner, Processor e Lootr."""
from PIL import Image, ImageDraw

W, H = 288, 206
PANEL_DIV_X = 88      # divisor painel lateral / area principal
RIGHT_DIV_X = 260     # divisor area principal / coluna direita
MAIN_X = 94
PLAYER_Y = 122
HOTBAR_Y = 180
FUEL = (265, 80)
ENERGY = (268, 18, 12, 54)  # x, y, w, h

THEMES = {
    "miner": {
        "bg":        (11, 14, 22, 255),    # fundo geral
        "panel":     (13, 18, 32, 255),    # painel lateral
        "main":      (14, 17, 26, 255),    # area principal
        "right":     (12, 16, 28, 255),    # coluna direita
        "border_lt": (42, 56, 88, 255),
        "border_dk": (4, 6, 12, 255),
        "slot_bg":   (8, 10, 16, 255),
        "slot_lt":   (30, 42, 68, 255),
        "slot_dk":   (3, 4, 8, 255),
        "frame":     (26, 36, 60, 255),
    },
    "processor": {
        "bg":        (18, 13, 7, 255),
        "panel":     (24, 17, 9, 255),
        "main":      (21, 15, 9, 255),
        "right":     (22, 15, 8, 255),
        "border_lt": (74, 58, 32, 255),
        "border_dk": (8, 5, 2, 255),
        "slot_bg":   (12, 8, 4, 255),
        "slot_lt":   (58, 42, 22, 255),
        "slot_dk":   (5, 3, 1, 255),
        "frame":     (58, 42, 16, 255),
    },
    "lootr": {
        "bg":        (18, 12, 22, 255),
        "panel":     (24, 14, 30, 255),
        "main":      (20, 12, 26, 255),
        "right":     (22, 12, 28, 255),
        "border_lt": (72, 40, 88, 255),
        "border_dk": (4, 2, 6, 255),
        "slot_bg":   (10, 6, 14, 255),
        "slot_lt":   (56, 30, 68, 255),
        "slot_dk":   (4, 2, 5, 255),
        "frame":     (56, 30, 60, 255),
    },
}


def slot(d, t, x, y):
    """Caixa de slot 18x18 com item em (x, y) — borda em (x-1, y-1)."""
    d.rectangle([x - 1, y - 1, x + 17, y + 17], fill=t["slot_bg"])
    # sombra em cima/esquerda, luz embaixo/direita (rebaixado)
    d.line([x - 1, y - 1, x + 16, y - 1], fill=t["slot_dk"])
    d.line([x - 1, y - 1, x - 1, y + 16], fill=t["slot_dk"])
    d.line([x - 1, y + 17, x + 17, y + 17], fill=t["slot_lt"])
    d.line([x + 17, y - 1, x + 17, y + 17], fill=t["slot_lt"])


def base(theme):
    t = THEMES[theme]
    img = Image.new("RGBA", (W, H), t["bg"])
    d = ImageDraw.Draw(img)

    # regioes
    d.rectangle([2, 2, PANEL_DIV_X - 1, H - 3], fill=t["panel"])
    d.rectangle([PANEL_DIV_X + 2, 2, RIGHT_DIV_X - 1, H - 3], fill=t["main"])
    d.rectangle([RIGHT_DIV_X + 2, 2, W - 3, H - 3], fill=t["right"])

    # borda externa (relevo)
    d.rectangle([0, 0, W - 1, H - 1], outline=t["border_dk"])
    d.line([1, 1, W - 2, 1], fill=t["border_lt"])
    d.line([1, 1, 1, H - 2], fill=t["border_lt"])
    d.line([1, H - 2, W - 2, H - 2], fill=t["border_dk"])
    d.line([W - 2, 1, W - 2, H - 2], fill=t["border_dk"])

    # divisores verticais
    for dx in (PANEL_DIV_X, RIGHT_DIV_X):
        d.line([dx, 2, dx, H - 3], fill=t["border_dk"])
        d.line([dx + 1, 2, dx + 1, H - 3], fill=t["border_lt"])

    # inventario do jogador
    for row in range(3):
        for col in range(9):
            slot(d, t, MAIN_X + col * 18, PLAYER_Y + row * 18)
    for col in range(9):
        slot(d, t, MAIN_X + col * 18, HOTBAR_Y)

    # combustivel + moldura da barra de energia
    slot(d, t, *FUEL)
    ex, ey, ew, eh = ENERGY
    d.rectangle([ex - 1, ey - 1, ex + ew, ey + eh], fill=t["slot_bg"])
    d.rectangle([ex - 1, ey - 1, ex + ew, ey + eh], outline=t["frame"])

    return img, d, t


def gen_miner(path):
    img, d, t = base("miner")
    # grade de saida 9x3 em y=20
    for row in range(3):
        for col in range(9):
            slot(d, t, MAIN_X + col * 18, 20 + row * 18)
    # melhorias (3, espacadas de 22) + mundo + encantamento em y=90
    for i in range(3):
        slot(d, t, MAIN_X + i * 22, 90)
    slot(d, t, 200, 90)
    slot(d, t, 238, 90)
    img.save(path)


def gen_processor(path):
    # slots da area principal sao desenhados dinamicamente pela screen (variam por tier)
    img, d, t = base("processor")
    img.save(path)


def gen_lootr(path):
    # slots desenhados dinamicamente; apenas fundo + inventario + combustivel no texto
    img, d, t = base("lootr")
    img.save(path)


if __name__ == "__main__":
    import sys
    out = sys.argv[1]
    gen_miner(f"{out}/avoid_miner.png")
    gen_processor(f"{out}/avoid_processor.png")
    gen_lootr(f"{out}/avoid_lootr.png")
    print("ok")
