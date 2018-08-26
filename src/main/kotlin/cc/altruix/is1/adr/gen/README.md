# Amazon Ad Companion: Generative Models

## Purpose of this document

To document, how we may create ads based on data on performance
of ads we ran previously.

## Theoretical basis

* [Learning to Generate Reviews and Discovering Sentiment](https://arxiv.org/pdf/1704.01444.pdf): the
 description of the approach I want to use. This file is available in the
 `doc/cc/altruix/is1/adr/gen/1704.01444.pdf`.
* [The Unreasonable Effectiveness of Recurrent Neural Networks](http://karpathy.github.io/2015/05/21/rnn-effectiveness/): A
 more accessible text on RNNs. Available in the above documentation
 directory as `unreasonable.pdf`.
* [Auto-Generating Clickbait With Recurrent Neural Networks](https://larseidnes.com/2015/10/13/auto-generating-clickbait-with-recurrent-neural-networks/):
 Judging from the title, it may be similar to what we need. Available in the above documentation directory as
 `clickbait.pdf`
* [Multiplicative LSTM for sequence modelling](https://arxiv.org/pdf/1609.07959.pdf), very theoretical, file `1609.07959.pdf`.
* [Supervised Sequence Labelling with Recurrent Neural Networks](http://www.cs.toronto.edu/~graves/phd.pdf), file
 `phd.pdf`. Fundamental text by the inventor of RNNs (sort of).
* [Recurrent Neural Networks](https://metacademy.org/graphs/concepts/recurrent_neural_networks) - some introduction.
* [Understanding LSTM Networks](http://colah.github.io/posts/2015-08-Understanding-LSTMs/), file `2015-08-Understanding-LSTMs.pdf`.
* [RECURRENT NEURAL NETWORKS TUTORIAL, PART 1 – INTRODUCTION TO RNNS](http://www.wildml.com/2015/09/recurrent-neural-networks-tutorial-part-1-introduction-to-rnns/), file `recurrent-neural-networks-tutorial-part-1-introduction-to-rnns.pdf`.

## Thinking Protocol (2017-06-08)

* TODO: The next step is to find out the topology used by generators of positive reviews.

## Topology determination

* See `doc/cc/altruix/is1/adr/gen/2017_06_09_topology`.

* Relevant parts from [Learning to Generate Reviews and Discovering Sentiment](https://arxiv.org/pdf/1704.01444.pdf):

> a single layer multiplicative LSTM (Krause et al., 2016)
> with 4096 units.

[...]

> The model was trained for a single epoch
> on mini-batches of 128 subsequences of length 256 for a
> total of 1 million weight updates. States were initialized
> to zero at the beginning of each shard and persisted across
> updates to simulate full-backpropagation and allow for the
> forward propagation of information outside of a given subsequence.

[...]

> Adam (Kingma & Ba, 2014) was used to accelerate
> learning with an initial 5e-4 learning rate that was
> decayed linearly to zero over the course of training.

[...]

> Weight normalization (Salimans & Kingma, 2016) was applied to
> the LSTM parameters. Data-parallelism was used across 4
> Pascal Titan X gpus to speed up training and increase effective
> memory size. Training took approximately one month.

> Our model processes text as a sequence of UTF-8 encoded
> bytes (Yergeau, 2003). For each byte, the model updates its
> hidden state and predicts a probability distribution over the
> next possible byte. The hidden state of the model serves
> as an online summary of the sequence which encodes all
> information the model has learned to preserve that is relevant
> to predicting the future bytes of the sequence. We are
> interested in understanding the properties of the learned encoding.
> The process of extracting a feature representation
> is outlined as follows:
> * Since newlines are used as review delimiters in the
> training dataset, all newline characters are replaced
> with spaces to avoid the model resetting state.
> *  Any leading whitespace is removed and replaced with
> a newline+space to simulate a start token. Any trailing
> whitespace is removed and replaced with a space to
> simulate an end token. The text is encoded as a UTF-
> 8 byte sequence.
> *  Model states are initialized to zeros. The model processes
> the sequence and the final cell states of the mLSTM
> are used as a feature representation. Tanh is applied
> to bound values between -1 and 1.



### Questions

#### Sentiment unit

> We were curious whether a similar result could be
> achieved using the sentiment unit. In Table 5 we show that
> by simply setting the sentiment unit to be positive or negative,
> the model generates corresponding positive or negative
> reviews. While all sampled negative reviews contain
> sentences with negative sentiment, they sometimes contain
> sentences with positive sentiment as well.

1. How do I find the sentiment unit?
2. How do I set it?

#### How do I tell the network that ad text A that was shown to audience B times (impressions) looking for keywords C generated D clicks?

1. Hold the keywords fixed, and include only impressions and clicks.


## Ideas

* Use several mechanisms for determining optimal keywords and optimal texts.


## Thinking protocol

Imagine, I have a neural network, which takes

1. an ad text as input and
2. determines the number of clicks it generated.

## Message to Reddit

### Design of a neural network for supervised text learning

Hello!

I'm trying to understand how to design a LSTM/RNN network that will help me write better ads based on past data.

The inputs are tuples with

1. a text of the ad (max. 150 characters) and
2. the reward (e. g. number of clicks that ad generated).

The reward is a measure of the ad's performance.

As far as I understand, the network has to have 2 inputs:

a) Current character
b) The reward of the current ad

When I train the network, I split the ad text into individual characters and then feed the tuples (character, estimate reward so far) into the network so that it learns to

a) predict the next character and
b) the peformance of the ad.

Graphical

I took the idea of this design from the description of a neural network that generates [clickbait](https://larseidnes.com/2015/10/13/auto-generating-clickbait-with-recurrent-neural-networks/). However, the network presented in this article didn't take into account information regarding the performance of the text. Therefore I'm not sure whether or not I'm thinking in the right direction.

Question: If I want to build a neural network that would generate good ads what inputs and outputs should it have? Are there other, much better approaches than the one I described?

Thanks in advance

Dmitri Pisarenko


### Message to Reddit v. 2.

How to design a neural network that generates good ads and is trained using supervised learning?

Hello!

I'm trying to understand how to design a LSTM/RNN network that will help me write better ads based on past data.

The training data are tuples with

1. a text of the ad (max. 150 characters) and
2. the reward (e. g. number of clicks that ad generated).

What is a reasonable design (topology, input and output layers) for such neural network?

I came up with two different approaches (see below) and both of them are problematic.

*Approach 1*



## TRIZ analysis

### Contradiction

1. We need to learn the data character by character in order to be able to generate ads.
2. We need to ads as a whole in order to learn the rewards (i. e. so that the network learns what ads perform best).

### Idea registry

#### Combine sliding window and reward data in training patterns

New! Read this awesome book and learn about the adventures of its heroes and heroines. Grab it NOW at a reduced price!


Imagine we want to teach the network that the ad with text "Hello" had a reward of 20.

Let's assume that the sequence length is 100.

We create following training samples (";" is the separator):

1) Training sample 1

Input: "New! Read this awesome book and learn about the adventures of its heroes and heroines. Grab it NOW "

Output 1: "a"
Output 2: 20

2) Training sample 2

Input: "ew! Read this awesome book and learn about the adventures of its heroes and heroines. Grab it NOW a"

Output 1: "t"
Output 2: 20

3) Training sample 3

Input: "w! Read this awesome book and learn about the adventures of its heroes and heroines. Grab it NOW at"

Output 1: " "
Output 2: 20


## Message to Reddit v. 3

http://machinelearningmastery.com/text-generation-lstm-recurrent-neural-networks-python-keras/


How to teach LSTM network to write good ad copy

Can you feed to a LSTM network inputs other than text?

Hi!

I want to build a neural network that would create ad texts for me. I want to use the approach presented in Jason Brownlee's article [Text Generation With LSTM Recurrent Neural Networks in Python with Keras](http://machinelearningmastery.com/text-generation-lstm-recurrent-neural-networks-python-keras/).

However, I want my network to take into account the performance of the ad, i. e. it should learn that ad X generated Y clicks (or sales).

In the above article the author takes a large text and transforms it into tuples that contain a sequence of characters and the character following it. If the size of the sliding window is 5, the text

> The amount of energy necessary to refute bullshit is an order of magnitude bigger than to produce it.

would be transformed in tuples

* ("The a", "m")
* ("he am", "o")
* ("e amo", "u")

and so on (first part of the tuple is the input to the network, second - the correct result for that input).

In order for the network to learn not only the structure of the language, but also the performance of the individual ads, I want to use different training data. Imagine, we have following ads:

* Ad 1, text: "Super-duper product", clicks: 30
* Ad 2, text: "Awesome product", clicks: 26

I would transform both ads in tuples

* ("Super", 30, "-")
* ("uper-", 30, "d")
* ("per-d", 30, "u")
...
* ("oduct", 30, END_MARKER)
* ("Aweso", 26, "m")
* ("wesom", 26, "e")
* ("esome", 26, " ")
...
* ("oduct", 26, END_MARKER)

where the first parameter is the text input, the second - input representing the performance of the ad, and the last part is the correct result (next character or END_MARKER if the end of the ad is reached).

**Generating texts**

After the network has been trained, I generate a text by

a) feeding a random (but meaningful) sequence of characters to one input and
b) the desired quality of the ad (the higher, the more similar the output will be to the high-performing ads).

**Question**

Can this adaptation to the above design work in theory?

I understand that I only can find out, how this performs by actually building the network. What I'm interested right now is whether there are obvious design errors.

Many thanks in advance

Dmitri Pisarenko