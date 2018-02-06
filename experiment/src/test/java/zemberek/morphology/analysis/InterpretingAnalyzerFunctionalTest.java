package zemberek.morphology.analysis;

import java.util.Comparator;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import zemberek.morphology.analyzer.AnalysisResult;
import zemberek.morphology.analyzer.InterpretingAnalyzer;
import zemberek.morphology.analyzer.InterpretingAnalyzer.AnalysisDebugData;
import zemberek.morphology.analyzer.MorphemeSurfaceForm;
import zemberek.morphology.lexicon.RootLexicon;
import zemberek.morphology.lexicon.tr.TurkishDictionaryLoader;

public class InterpretingAnalyzerFunctionalTest {

  private static InterpretingAnalyzer getAnalyzer(String... dictionaryLines) {
    RootLexicon loader = new TurkishDictionaryLoader().load(dictionaryLines);
    return new InterpretingAnalyzer(loader);
  }

  private boolean containsMorpheme(AnalysisResult result, String morphemeName) {
    for (MorphemeSurfaceForm forms : result.getMorphemes()) {
      if (forms.lexicalTransition.to.morpheme.id.equalsIgnoreCase(morphemeName)) {
        return true;
      }
    }
    return false;
  }

  private void printAndSort(String input, List<AnalysisResult> results) {
    results.sort(Comparator.comparing(AnalysisResult::toString));
    for (AnalysisResult result : results) {
      System.out.println(input + " = " + result);
    }
  }

  @Test
  public void shouldParse_1() {
    String in = "elmalar";
    List<AnalysisResult> results = getAnalyzer("elma").analyze(in);
    printAndSort(in, results);
    Assert.assertEquals(1, results.size());
    AnalysisResult first = results.get(0);

    Assert.assertEquals("elma_Noun", first.getDictionaryItem().id);
    Assert.assertEquals("elma", first.root);
    Assert.assertTrue(containsMorpheme(first, "A3pl"));
  }

  @Test
  public void implicitDative_1() {
    String in = "içeri";
    List<AnalysisResult> results = getAnalyzer("içeri [A:ImplicitDative]")
        .analyze(in);
    printAndSort(in, results);
    Assert.assertEquals(2, results.size());
    AnalysisResult first = results.get(0);

    Assert.assertEquals("içeri_Noun", first.getDictionaryItem().id);
    Assert.assertEquals("içeri", first.root);
    Assert.assertTrue(containsMorpheme(first, "Dat"));
  }


  @Test
  public void voicing_1() {
    InterpretingAnalyzer analyzer = getAnalyzer("kitap");
    String in = "kitabım";
    List<AnalysisResult> results = analyzer.analyze(in);
    printAndSort(in, results);
    Assert.assertEquals(1, results.size());
    AnalysisResult first = results.get(0);

    Assert.assertEquals("kitap", first.getDictionaryItem().lemma);
    Assert.assertEquals("kitab", first.root);
    Assert.assertTrue(containsMorpheme(first, "P1sg"));
  }

  @Test
  public void voicingIncorrect_1() {
    InterpretingAnalyzer analyzer = getAnalyzer("kitap");
    shouldNotPass(analyzer, "kitapım", "kitab", "kitabcık", "kitapa", "kitablar");
  }

  @Test
  public void noun2Noun_1() {
    InterpretingAnalyzer analyzer = getAnalyzer("kitap");
    String in = "kitapçık";
    List<AnalysisResult> results = analyzer.analyze(in);
    printAndSort(in, results);
    Assert.assertEquals(1, results.size());
    AnalysisResult first = results.get(0);
    Assert.assertTrue(containsMorpheme(first, "Dim"));
  }

  @Test
  public void analysisWithDebug() {
    InterpretingAnalyzer analyzer = getAnalyzer("elma", "el", "elmas");
    String in = "elması";
    AnalysisDebugData debug = new AnalysisDebugData();
    List<AnalysisResult> results = analyzer.analyze(in, debug);
    debug.dumpToConsole();
    printAndSort(in, results);
  }


  public void shouldNotPass(InterpretingAnalyzer analyzer, String... words) {
    for (String word : words) {
      List<AnalysisResult> results = analyzer.analyze(word);
      if (results.size() != 0) {
        printAndSort(word, results);
        Assert.fail(word + " is expected to fail but passed.");
      }
    }
  }

  @Test
  public void noun2NounIncorrect_1() {
    InterpretingAnalyzer analyzer = getAnalyzer("kitap");
    shouldNotPass(analyzer, "kitaplarcık", "kitapçıklarcık",
        "kitapçığ", "kitapcık", "kitabımcık",
        "kitaptacık", "kitapçıkçık"
    );
  }


}
