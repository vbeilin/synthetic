package vb.synthetic;

import vb.synthetic.anno.Synthetic;

public class UnderTest {
    @Synthetic
    public String synthetic(String a) {
        return a;
    }
    
    public String normal(String a) {
        return a;
    }
}