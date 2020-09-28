package com.example.lilwiki

class Subsection(val title : String) {
    var texts = mutableListOf<String>()
    var latexs = mutableListOf<String>()
    fun appendText(text : String) {
        texts.add(text)
    }
    fun appendLatex(latex : String) {
        latexs.add(latex)
    }
}

class Article(val title : String) {
    var subsections = mutableListOf<Subsection>()
    fun appendSubsection(subsection: Subsection) {
        subsections.add(subsection)
    }
}

class Branch(val title : String) {
    var articles = mutableListOf<Article>()
    fun appendArticle(article: Article) {
        articles.add(article)
    }
}

class Discipline(val title : String) {
    var branches = mutableListOf<Branch>()
    fun appendBranch(branch: Branch) {
        branches.add(branch)
    }
}

object TempRep {
    var disciplines = mutableListOf<Discipline>()
    fun appendDiscipline(discipline: Discipline) {
        disciplines.add(discipline)
    }
    init {
        val subsection1 = Subsection("definition")
        subsection1.appendText("common infection")
        subsection1.appendLatex("$ ax^2+bx+c=0$")
        val subsection2 = Subsection("example")
        subsection2.appendText("one")
        subsection2.appendText("two")
        val article = Article("Square equation")
        article.appendSubsection(subsection1)
        article.appendSubsection(subsection2)
        val branch = Branch("Algebra")
        branch.appendArticle(article)
        val discipline = Discipline("Math")
        discipline.appendBranch(branch)
        disciplines.add(discipline)
        disciplines.add(Discipline("English"))
    }
}