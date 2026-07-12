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
    """Camara de invocacao: painel lateral com janela do mob 3D (y66..196),
    cartao dourado em (100,90) alimentando 3 melhorias dedicadas (194/216/238)."""
    img, d, t = base("lootr")
    gold = (170, 130, 50, 255)
    gold_dk = (90, 68, 26, 255)
    accent = (120, 70, 170, 255)

    # grade de saida 9x3 em y=20
    for row in range(3):
        for col in range(9):
            slot(d, t, MAIN_X + col * 18, 20 + row * 18)

    # slot do cartao com moldura decorativa dupla (dourada) e cantos marcados
    slot(d, t, 100, 90)
    d.rectangle([96, 86, 121, 111], outline=gold_dk)
    for cx in (96, 121):
        for cy in (86, 111):
            dx = 4 if cx == 96 else -4
            dy = 4 if cy == 86 else -4
            d.line([cx, cy, cx + dx, cy], fill=gold)
            d.line([cx, cy, cx, cy + dy], fill=gold)

    # chevrons cartao -> melhorias (fluxo de invocacao)
    ch_y = 98
    for i, ch_x in enumerate((132, 148, 164, 180)):
        c = accent if i % 2 == 0 else gold_dk
        d.line([ch_x, ch_y - 4, ch_x + 4, ch_y], fill=c, width=2)
        d.line([ch_x + 4, ch_y, ch_x, ch_y + 4], fill=c, width=2)

    # 3 slots dedicados de melhoria
    for i in range(3):
        slot(d, t, 194 + i * 22, 90)

    # camara do mob no painel lateral: janela funda com vinheta e cantos em L
    cx0, cy0, cx1, cy1 = 6, 66, 82, 196
    d.rectangle([cx0 - 2, cy0 - 2, cx1 + 1, cy1 + 1], fill=t["border_dk"])
    d.rectangle([cx0 - 1, cy0 - 1, cx1, cy1], fill=(6, 3, 9, 255))
    # vinheta simples: bordas internas levemente mais claras (profundidade)
    d.rectangle([cx0, cy0, cx1 - 1, cy1 - 1], outline=(16, 9, 22, 255))
    d.rectangle([cx0 + 1, cy0 + 1, cx1 - 2, cy1 - 2], outline=(11, 6, 16, 255))
    # piso da camara (pedestal)
    d.rectangle([cx0 + 6, cy1 - 10, cx1 - 7, cy1 - 8], fill=(30, 16, 40, 255))
    d.rectangle([cx0 + 12, cy1 - 8, cx1 - 13, cy1 - 6], fill=(20, 11, 28, 255))
    # cantos em L dourados
    L = 7
    for px, py, dx, dy in ((cx0, cy0, 1, 1), (cx1 - 1, cy0, -1, 1),
                           (cx0, cy1 - 1, 1, -1), (cx1 - 1, cy1 - 1, -1, -1)):
        d.line([px, py, px + dx * L, py], fill=gold)
        d.line([px, py, px, py + dy * L], fill=gold)

    # divisores do painel: abaixo do titulo e entre status e camara
    d.line([4, 17, 83, 17], fill=t["border_lt"])
    d.line([4, 58, 83, 58], fill=t["border_lt"])

    img.save(path)


if __name__ == "__main__":
    import sys
    out = sys.argv[1]
    gen_miner(f"{out}/avoid_miner.png")
    gen_processor(f"{out}/avoid_processor.png")
    gen_lootr(f"{out}/avoid_lootr.png")
    print("ok")
