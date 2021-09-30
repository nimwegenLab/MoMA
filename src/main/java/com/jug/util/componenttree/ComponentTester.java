package com.jug.util.componenttree;


import net.imglib2.Localizable;
import net.imglib2.algorithm.componenttree.Component;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ComponentTester<T extends Type<T>, C extends Component<T, C>>
        implements
        IComponentTester<T, C> {

    private List<ILocationTester> testers;

    public ComponentTester(List<ILocationTester> testers) {
        this.testers = testers;
    }

    @Override
    public boolean IsValid(C component) {
        Iterator<Localizable> pixelPositionIterator = component.iterator();
        while (pixelPositionIterator.hasNext()) {
            Localizable location = pixelPositionIterator.next();
            if(!TestersReturnValid(location)){
                ResetTesters();
                return false;
            }
        }
        ResetTesters();
        return true;
    }


    private boolean TestersReturnValid(Localizable location){
        for(ILocationTester tester: testers){
            if(!tester.IsValid(location)){
                return false;
            }
        }
        return true;
    }

    private void ResetTesters(){
        for(ILocationTester tester: testers){
            tester.Reset();
        }
    }
}
