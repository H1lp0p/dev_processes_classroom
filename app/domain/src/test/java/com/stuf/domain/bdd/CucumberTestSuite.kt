package com.stuf.domain.bdd

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["src/test/resources/features"],
    glue = ["com.stuf.domain.bdd"],
    plugin = ["pretty", "summary"],
    snippets = CucumberOptions.SnippetType.CAMELCASE,
)
class CucumberTestSuite
