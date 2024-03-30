# Solving Picross using Constraint-Programming tools

[![Generic badge](https://img.shields.io/badge/PICROSS-DONE-chartreuse.svg)](https://shields.io/)
- Creation of a _generic_ picross instance, loaded with ad-hoc `.px` files.
- Creation of a picross-solver class, taking the `.px` file location as an input
- First version consisted in enumeration of each solution for each row and each column.
- Current versions tries to capture reasoning done by humans.

- 🐖 : PIG (**P**icross **I**nstance **G**enerator), small python module creating `.px` instances.
- 🧠 : Writing a **checker**, ensuring every solution is correct

[![Generic badge](https://img.shields.io/badge/PICROSS-FIXME-orange.svg)](https://shields.io/)
- ⚔️ : First version of the solver works well for middle-size grids, but fails to load whenever too many tuples are possible.
- 🤔 : Current version does not (yet) capture every human reasoning, but bigger instances can be loaded. (Roughly every 15x15 grid can be solved)

[![Generic badge](https://img.shields.io/badge/PICROSS-TODO-informational.svg)](https://shields.io/)
- ↪️ : Propagation of the constraints, looking for the level of consistency of our model.
- 🚅 : Benchmarking : How many nodes are used ? How many tuples were enumerated ?

[![Generic badge](https://img.shields.io/badge/PICROSS-NEXT-8A2BE2.svg)](https://shields.io/)
- ⁉️ : Is there a model with the same consistencies, but using less tuples ?
-   1. This can be done using DFAs (cf. [Choco solver](https://choco-solver.org/tutos/nonogram/), however there is no DFA / regular on CPLEX).
- ✍️ : Writing a small report on the whole process (cf. [Overleaf](https://fr.overleaf.com/read/bxgrsftxdxhn#3cfc43) ).

[![Generic badge](https://img.shields.io/badge/PICROSS-MATHS_NEXT-8A2BE2.svg)](https://shields.io/)
- Algebraic formulation for a picross instance
