#!/usr/bin/env python3
"""
    Picross Instance Generator : 🐖
"""

import matplotlib.pyplot as plt
import re

from sys import argv

def initFigure(nb_rows : int, nb_cols : int):
    fig, ax = plt.subplots()
    ax.axis("square")

    for c in range(nb_cols + 1):
        ax.plot([c, c], [0, nb_rows], "k", linewidth = 0.2)
    
    for r in range(nb_rows + 1):
        ax.plot([0, nb_cols], [r, r], "k", linewidth = 0.2)

    ax.set_xlim(-1, nb_cols + 1)
    ax.set_ylim(-1, nb_rows + 1)
    ax.set_xticks([])
    ax.set_yticks([])
    return fig, ax

def getSquareFromIndexes(indLgn, indCol, nbRows):
    return [indCol, indCol + 1, indCol + 1, indCol],\
         [nbRows - 1 - indLgn, nbRows - 1 - indLgn,\
            nbRows - indLgn, nbRows - indLgn]

def fillsquare(indLgn, indCol, nbRows, color):
    xx, yy = getSquareFromIndexes(indLgn, indCol, nbRows)
    fx = xx[0]
    fy = yy[0]
    new_xx = [fx, fx + 1, fx + 1, fx]
    new_yy = [fy, fy, fy + 1, fy + 1]
    plt.fill(new_xx, new_yy, color = color)

def main():
    print("+++ 🐖 PIG : Picross Instance Generator 🐖 +++")
    try:
        NB_ROWS, NB_COLS, OUTPUT_FILENAME = int(argv[-3]), int(argv[-2]), argv[-1]
    except IndexError as ie:
        print("Index Error : Missing Argument")
        print("./pig.py NB_ROWS NB_COLS FILENAME_OUTPUT")
        print(ie)
        return
    except ValueError as va:
        print("Value Error : Incorrect Arguments")
        print("./pig.py NB_ROWS NB_COLS FILENAME_OUTPUT")
        print(va)
        return

    print(f"Creating a picross grid with {NB_ROWS} rows and {NB_COLS} columns.")
    print(f"File will be saved @{OUTPUT_FILENAME}")
    
    fig, ax = initFigure(NB_ROWS, NB_COLS)
    plt.suptitle("PIG : Picross Instance Generator")
    plt.title(f"Instance generated : {OUTPUT_FILENAME}")
    # img = plt.imread("godzilla.png")
    # ax.imshow(img, extent = [0, NB_COLS, 0, NB_ROWS])
    
    GRID = [[0 for _ in range(NB_COLS)] for __ in range(NB_ROWS)]
    while True:
        newpts = plt.ginput(1, timeout = -1)[0]
        cond1 = (newpts[0] < 0) or (newpts[0] > NB_COLS)
        cond2 = (newpts[1] < 0) or (newpts[1] > NB_ROWS)
        if (cond1 or cond2):
            break

        
        row = NB_ROWS - 1 - int(newpts[1])
        col = int(newpts[0])
        GRID[row][col] = 1 - GRID[row][col]
        if GRID[row][col] == 1:
            fillsquare(row,col,NB_ROWS,"black")
        else:
            fillsquare(row,col,NB_ROWS,"white")

    with open(OUTPUT_FILENAME, "w") as file:
        file.write(f"{NB_ROWS},{NB_COLS}\n")
        for row in GRID:
            row_str = "".join([str(v) for v in row])
            lengths_row = [len(s) for s in list(re.findall(r"1+", row_str))]
            assert len(lengths_row) > 0 
            if len(lengths_row) == 1:
                file.write(str(lengths_row[0]))
            else:
                for i, v in enumerate(lengths_row):
                    file.write(str(v))
                    if i < len(lengths_row) - 1:
                        file.write(",")
            file.write("\n")
    
        tGRID = [[GRID[i][j] for i in range(NB_ROWS)] for j in range(NB_COLS)]
        for col in tGRID:
            col_str = "".join([str(v) for v in col])
            lengths_col = [len(s) for s in list(re.findall(r"1+", col_str))]
            assert len(lengths_col) > 0
            if len(lengths_col) == 1:
                file.write(str(lengths_col[0]))
            else:
                for j, w in enumerate(lengths_col):
                    file.write(str(w))
                    if j < len(lengths_col) - 1:
                        file.write(",")
            file.write("\n")

        file.close()

if __name__ == "__main__":
    main()