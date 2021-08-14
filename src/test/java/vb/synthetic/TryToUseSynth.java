package vb.synthetic;

public class TryToUseSynth {
    public String a() {
        UnderTest underTest = new UnderTest();
        return "compiled-" + underTest.synthetic("1") + "-" + underTest.normal("2");
    }
}
