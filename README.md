# Huffman Encoding
This project is a [Huffman encoding](https://en.wikipedia.org/wiki/Huffman_coding) program, created for problem 3 in [this assignment](https://course.ccs.neu.edu/cs2510a/assignment6.html) for [this course](https://course.ccs.neu.edu/cs2510a/index.html).

Given an alphabet and the relative frequencies of each letter, it creates a binary code for each letter, such that letters that show up more in the language (have larger frequencies) will have shorter codes, while letters that show up less often in the language (have smaller frequencies) will have longer codes. When using a code to represent a word in a language, on average, a code produced through Huffman encoding will be shorter than an encoding algorithm that gives every letter a code with the same length.

## What are tester.jar and javalib.jar?
I use `tester.jar`, a tester library, and `javalib.jar`, an image library, both built by [my professor](https://www.khoury.northeastern.edu/people/benjamin-lerner/)! To use these libraries, include them in whatever project contains these .java files as an external jar. To run the program, set your run configurations to use `tester.Main` as the main class, with the name of the `ExamplesAutomata` class as the program argument. 
