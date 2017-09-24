# vocab-to-graphviz

Visualize RDF vocabularies via [Graphviz](http://www.graphviz.org).

## Usage

Compile using [Leiningen](http://leiningen.org) and [lein-binplus](https://github.com/BrunoBonacci/lein-binplus):

```bash
git clone https://github.com/jindrichmynarz/vocab-to-graphviz
cd vocab-to-graphviz
lein bin
```

The tool takes a file describing an RDF vocabulary specified by the `-i` argument and outputs a class diagram of the vocabulary described in the [DOT language](http://www.graphviz.org/doc/info/lang.html). The input can be written in one of the RDF serializations recognized by the [file extension](https://jena.apache.org/documentation/io/#command-line-tools). The output is saved to a file given by the `-o` argument (or standard output by default):

```bash
target/vocab_to_graphviz -i vocabulary.ttl -o vocabulary.dot
```

For example, if we try it on the [Public Contracts Ontology](https://github.com/opendatacz/public-contracts-ontology) and render it via Graphviz, we can get this:

![Example diagram](https://github.com/jindrichmynarz/vocab-to-graphviz/blob/master/examples/pco.png)

Clearly, this diagram leaves much to be desired, so we can render it to SVG and tweak its layout to obtain this:

![Cleaned diagram](https://github.com/jindrichmynarz/vocab-to-graphviz/blob/master/examples/pco_cleaned.png)

## License

Copyright © 2017 Jindřich Mynarz

Distributed under the Eclipse Public License version 1.0. 
