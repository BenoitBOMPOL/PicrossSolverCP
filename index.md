---
layout: default
---


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

Text can be **bold**, _italic_, or ~~strikethrough~~.

[Link to another page](./another-page.html).

There should be whitespace between paragraphs.

There should be whitespace between paragraphs. We recommend including a README, or a file with information about your project.

# Header 1

This is a normal paragraph following a header. GitHub is a code hosting platform for version control and collaboration. It lets you and others work together on projects from anywhere.

## Header 2

> This is a blockquote following a header.
>
> When something is important enough, you do it even if the odds are not in your favor.

### Header 3

```js
// Javascript code with syntax highlighting.
var fun = function lang(l) {
  dateformat.i18n = require('./lang/' + l)
  return true;
}
```

```ruby
# Ruby code with syntax highlighting
GitHubPages::Dependencies.gems.each do |gem, version|
  s.add_dependency(gem, "= #{version}")
end
```

#### Header 4

*   This is an unordered list following a header.
*   This is an unordered list following a header.
*   This is an unordered list following a header.

##### Header 5

1.  This is an ordered list following a header.
2.  This is an ordered list following a header.
3.  This is an ordered list following a header.

###### Header 6

| head1        | head two          | three |
|:-------------|:------------------|:------|
| ok           | good swedish fish | nice  |
| out of stock | good and plenty   | nice  |
| ok           | good `oreos`      | hmm   |
| ok           | good `zoute` drop | yumm  |

### There's a horizontal rule below this.

* * *

### Here is an unordered list:

*   Item foo
*   Item bar
*   Item baz
*   Item zip

### And an ordered list:

1.  Item one
1.  Item two
1.  Item three
1.  Item four

### And a nested list:

- level 1 item
  - level 2 item
  - level 2 item
    - level 3 item
    - level 3 item
- level 1 item
  - level 2 item
  - level 2 item
  - level 2 item
- level 1 item
  - level 2 item
  - level 2 item
- level 1 item

### Small image

![Octocat](https://github.githubassets.com/images/icons/emoji/octocat.png)

### Large image

![Branching](https://guides.github.com/activities/hello-world/branching.png)


### Definition lists can be used with HTML syntax.

<dl>
<dt>Name</dt>
<dd>Godzilla</dd>
<dt>Born</dt>
<dd>1952</dd>
<dt>Birthplace</dt>
<dd>Japan</dd>
<dt>Color</dt>
<dd>Green</dd>
</dl>

```
Long, single-line code blocks should not wrap. They should horizontally scroll if they are too long. This line should be long enough to demonstrate this.
```

```
The final element.
```
