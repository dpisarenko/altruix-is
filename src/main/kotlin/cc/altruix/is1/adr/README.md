# Ad companion

## Purpose

* To create a system, which will help me write ads for Amazon ad platform.

Notes:

* Contrary to Facebook, Amazon shows ads slowly. In order to maximize sales per month it is therefore necessary to have
as many good ads as possible. That is, you cannot write one good ad and then spend all the money on it (Amazon won't
allow you to spend that money fast).

## Main idea

> If you want the whole process to be automated, and you have a lot of data,
> I'd suggest: https://arxiv.org/pdf/1704.01444.pdf . Instead of generating
> the most positive reviews like they did, generate the most profitable ads.
>
> However, seeing as you don't have a lot of data, and I'm not sure you
> want to automate everything, a simple n-grams TF-IDF linear regression
> should be decent: you can gain insight into which n-grams have the highest
> and lowest coefficients, and therefore, which n-grams are best and worst,
> respectively. With more data, your options really expand.

Source: https://www.reddit.com/r/MLQuestions/comments/6dz067/how_to_train_textgenerating_ai_system_with_little/di78n55/?context=3&utm_medium=message_notification&utm_source=email&utm_name=6c67084a55971487b0f5a21d3c7c96deb2d54526&utm_content=post_reply&utm_term=0

In `AmazonAdCompanion` I want to implement that TF-IDF linear regression.

## Reply to the guy with TD-IDF

Hello!

Thanks for your answer.


1) Let's assume we have the following ad performance tuples (the higher the real number, the better the ad performed).

	AdPerformanceTuple(
			"Ad-1",
			"Super-duper short story.",
			0.5
	),
	AdPerformanceTuple(
			"Ad-2",
			"A refugee drama in the heart of Europe.",
			0.6
	),
	AdPerformanceTuple(
			"Ad-3",
			"Wanna go on vacation to Europe? Read this thriller!",
			0.4
	),
	AdPerformanceTuple(
			"Ad-4",
			"Street fights! Foreign people! Gothic vaults! All this and more in the new short story X.",
			0.8
	)

2) Calculate TF (term frequency) and IDF (inverse document frequency) for all words of all ads (i. e. for "super-duper", "short", "story", "refugee", "drama", "heart", "Europe" etc.).

3) For every word, perform linear regression analysis.

3.a) Ad performance = Alpha + Beta * TF*IDF.

Example for term "story", TF approach: The term "story" appears in ads Ad-1 and Ad-4. We have following data to run the regression with:

3.1.1) Ad-1, TF("story", "Ad-1")*IDF("story"), ad performance = 0.5
3.1.1) Ad-4, TF("story", "Ad-4")*IDF("story"), ad performance = 0.8

From these data I calculate Alpha and Beta for the equation 3.a.

4) Find words with the highest coefficients.

## Useful links

* http://www.tfidf.com/
* http://lucene.apache.org/core/3_6_1/api/all/org/apache/lucene/search/Similarity.html
* [Simple regression](http://commons.apache.org/proper/commons-math/userguide/stat.html#a1.4_Simple_regression)