#!/usr/bin/env python3
"""Gera as texturas de GUI (288x206) do Avoid Miner, Processor e Lootr.

Todas seguem o mesmo esqueleto: painel lateral (0..88) com titulo/status e
uma "camara" (janela funda em (6,66)-(82,196)), area principal (92..258) e
coluna direita com energia/combustivel. Cada maquina tem um tema proprio:
miner = aco azulado, processor = cobre/bronze, lootr = void roxo.
"""
from PIL import Image, ImageDraw

W, H = 288, 206
PANEL_DIV_X = 88      # divisor painel lateral / area principal
RIGHT_DIV_X = 260     # divisor area principal / coluna direita
MAIN_X = 94
PLAYER_Y = 122
HOTBAR_Y = 180
FUEL = (265, 80)
ENERGY = (268, 18, 12, 54)  # x, y, w, h
CHAMBER = (6, 66, 82, 196)  # x0, y0, x1, y1 (janela do painel lateral)

THEMES = {
    "miner": {
        "bg":        (12, 15, 21, 255),
        "panel":     (17, 21, 30, 255),
        "main":      (14, 18, 26, 255),
        "right":     (15, 19, 28, 255),
        "border_lt": (56, 70, 100, 255),
        "border_dk": (4, 6, 12, 255),
        "slot_bg":   (8, 10, 16, 255),
        "slot_lt":   (40, 52, 80, 255),
        "slot_dk":   (3, 4, 8, 255),
        "frame":     (36, 48, 76, 255),
        "corner":    (110, 160, 220, 255),   # cantos da camara
        "rivet_lt":  (88, 104, 136, 255),
        "rivet_dk":  (28, 36, 52, 255),
    },
    "processor": {
        "bg":        (24, 15, 8, 255),
        "panel":     (32, 21, 11, 255),
        "main":      (28, 18, 10, 255),
        "right":     (30, 20, 10, 255),
        "border_lt": (108, 76, 38, 255),
        "border_dk": (8, 5, 2, 255),
        "slot_bg":   (14, 9, 5, 255),
        "slot_lt":   (84, 58, 28, 255),
        "slot_dk":   (6, 3, 1, 255),
        "frame":     (84, 58, 24, 255),
        "corner":    (214, 150, 84, 255),
        "rivet_lt":  (150, 106, 54, 255),
        "rivet_dk":  (46, 30, 14, 255),
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
        "corner":    (170, 130, 50, 255),    # dourado (cartao)
        "rivet_lt":  (96, 62, 112, 255),
        "rivet_dk":  (26, 14, 32, 255),
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


def rivet(d, t, x, y):
    """Rebite 2x2: pixel claro + sombra."""
    d.point([(x, y), (x + 1, y)], fill=t["rivet_lt"])
    d.point([(x, y + 1), (x + 1, y + 1)], fill=t["rivet_dk"])


def chamber(d, t, pedestal=False):
    """Janela funda do painel lateral com cantos em L na cor de destaque."""
    cx0, cy0, cx1, cy1 = CHAMBER
    d.rectangle([cx0 - 2, cy0 - 2, cx1 + 1, cy1 + 1], fill=t["border_dk"])
    d.rectangle([cx0 - 1, cy0 - 1, cx1, cy1], fill=(6, 5, 8, 255))
    # profundidade: bordas internas levemente mais claras
    inner1 = tuple(min(255, c + 9) for c in (6, 5, 8)) + (255,)
    d.rectangle([cx0, cy0, cx1 - 1, cy1 - 1], outline=inner1)
    if pedestal:
        d.rectangle([cx0 + 6, cy1 - 10, cx1 - 7, cy1 - 8], fill=t["slot_lt"])
        d.rectangle([cx0 + 12, cy1 - 8, cx1 - 13, cy1 - 6], fill=t["slot_bg"])
    # cantos em L
    L = 7
    for px, py, sx, sy in ((cx0, cy0, 1, 1), (cx1 - 1, cy0, -1, 1),
                           (cx0, cy1 - 1, 1, -1), (cx1 - 1, cy1 - 1, -1, -1)):
        d.line([px, py, px + sx * L, py], fill=t["corner"])
        d.line([px, py, px, py + sy * L], fill=t["corner"])


def panel_dividers(d, t):
    """Divisores do painel lateral: abaixo do titulo (y17) e do status (y58)."""
    d.line([4, 17, 83, 17], fill=t["border_lt"])
    d.line([4, 58, 83, 58], fill=t["border_lt"])


def plate_rivets(d, t):
    """Rebites nos cantos do painel lateral e da area principal."""
    for x, y in ((5, 5), (81, 5), (5, 199), (81, 199),
                 (93, 4), (254, 4), (93, 199), (254, 199)):
        rivet(d, t, x, y)


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


def hazard_band(d, x0, x1, y0, h=4):
    """Faixa de listras diagonais estilo maquina industrial (bem discreta)."""
    dark = (14, 12, 6, 255)
    amber = (96, 78, 20, 255)
    for x in range(x0, x1):
        for y in range(y0, y0 + h):
            c = amber if ((x + y) // 4) % 2 == 0 else dark
            d.point((x, y), fill=c)


def gen_miner(path):
    """Aco azulado: grade de saida, faixa de perigo, slots dedicados e
    camara de amostras (a screen desenha os itens possiveis dentro dela)."""
    img, d, t = base("miner")
    # grade de saida 9x3 em y=20
    for row in range(3):
        for col in range(9):
            slot(d, t, MAIN_X + col * 18, 20 + row * 18)
    # faixa de perigo entre saida e melhorias
    hazard_band(d, 93, 258, 79)
    # melhorias (3, espacadas de 22) + mundo + encantamento em y=90
    for i in range(3):
        slot(d, t, MAIN_X + i * 22, 90)
    slot(d, t, 200, 90)
    slot(d, t, 238, 90)
    chamber(d, t)
    panel_dividers(d, t)
    plate_rivets(d, t)
    img.save(path)


def gen_processor(path):
    """Cobre/bronze: viga aparafusada entre area de processo e melhorias,
    camara-catalogo de receitas; slots das colunas sao desenhados pela screen."""
    img, d, t = base("processor")
    # viga metalica horizontal com parafusos em y=84..88
    d.rectangle([93, 84, 257, 88], fill=(52, 36, 18, 255))
    d.line([93, 84, 257, 84], fill=t["border_lt"])
    d.line([93, 88, 257, 88], fill=t["border_dk"])
    for bx in range(100, 258, 22):
        rivet(d, t, bx, 85)
    chamber(d, t)
    panel_dividers(d, t)
    plate_rivets(d, t)
    img.save(path)


def gen_lootr(path):
    """Camara de invocacao: janela do mob 3D no painel, cartao dourado em
    (100,90) alimentando 3 melhorias dedicadas (194/216/238)."""
    img, d, t = base("lootr")
    gold = t["corner"]
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

    chamber(d, t, pedestal=True)
    panel_dividers(d, t)
    img.save(path)


if __name__ == "__main__":
    import sys
    out = sys.argv[1]
    gen_miner(f"{out}/avoid_miner.png")
    gen_processor(f"{out}/avoid_processor.png")
    gen_lootr(f"{out}/avoid_lootr.png")
    print("ok")
