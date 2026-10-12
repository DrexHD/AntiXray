#!/usr/bin/env python3
"""Execute the real extracted preset-loading method against a palette model.

The model retains duplicate loaded slots like LithiumHashPalette's constructor.
This is an algorithm regression, NOT a Minecraft/Fabric integration test.
"""
import argparse
from pathlib import Path
import subprocess
import tempfile

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('source', nargs='?', type=Path, default=(
    Path(__file__).resolve().parents[1]
    / 'common/src/main/java/me/drex/antixray/common/mixin/PalettedContainerMixin.java'
), help='Mixin source to test (defaults to this checkout)')
args=parser.parse_args()
text=args.source.read_text()
start=text.index('    private void addPresetValuesWithEntries(')
brace=text.index('{', start)
depth=1
end=brace+1
while depth:
    if text[end]=='{': depth+=1
    elif text[end]=='}': depth-=1
    end+=1
method=text[start:end]
java=r'''package net.minecraft.world.level.chunk;
import java.util.*;

interface PaletteResize<T> { int onResize(int bits,T value); }
class Strategy<T> {
    static final Object SINGLE_VALUE_PALETTE_FACTORY=new Object();
    static final Object HASH_FACTORY=new Object();
}
interface Configuration {
    int bitsInMemory();
    record Simple(Object factory,int bitsInMemory) implements Configuration {}
    record Global(int bitsInMemory) implements Configuration {}
}
class BitStorage {}
class CallbackInfo {}
class Blocks {
    static final Blocks AIR=new Blocks();
    Object defaultBlockState() { return "air"; }
}
class Mth {
    static int ceillog2(int n) { return n<=1 ? 0 : 32-Integer.numberOfLeadingZeros(n-1); }
}
class Arguments {
    static final ThreadLocal<List<?>> PALETTE_ENTRIES=new ThreadLocal<>();
}
class Palette<T> {
    final int bits;
    final List<T> entries;
    final Map<T,Integer> ids=new IdentityHashMap<>();
    Palette(int bits,List<T> loaded) {
        this.bits=bits;
        this.entries=new ArrayList<>(loaded);
        for (int i=0;i<loaded.size();i++) ids.put(loaded.get(i),i);
    }
    int getSize() { return entries.size(); }
    T valueFor(int i) { return entries.get(i); }
    int idFor(T value,PaletteResize<T> resize) {
        Integer existing=ids.get(value);
        if(existing!=null) return existing;
        int id=entries.size(); entries.add(value); ids.put(value,id);
        if(id >= 1<<bits) return resize.onResize(bits+1,value);
        return id;
    }
}
class PalettedContainer<T> implements PaletteResize<T> {
    record Data<T>(Palette<T> palette,Configuration configuration) {}
    Data<T> data;
    T[] antiXray$presetValues;
    void antiXray$initializePresetValues() {}
    PalettedContainer(int bits,List<T> entries,T[] presets) {
        data=new Data<>(new Palette<>(bits,entries),new Configuration.Simple(Strategy.HASH_FACTORY,bits));
        antiXray$presetValues=presets;
        Arguments.PALETTE_ENTRIES.set(entries);
    }
    public int onResize(int bits,T added) {
        List<T> unique=new ArrayList<>();
        for(T t:data.palette.entries) if(!unique.contains(t)) unique.add(t);
        Palette<T> p=new Palette<>(bits,unique);
        data=new Data<>(p,new Configuration.Simple(Strategy.HASH_FACTORY,bits));
        for(T t:antiXray$presetValues) p.idFor(t,this);
        return added==null ? -1 : data.palette.idFor(added,this);
    }
'''+method+r'''
    void loadPresets() {
        addPresetValuesWithEntries(new Strategy<>(),data.configuration,new BitStorage(),data.palette,new CallbackInfo());
    }
}
public class PaletteRegression {
    static void check(String label,int bits,List<String> entries,String... presets) {
        PalettedContainer<String> c=new PalettedContainer<>(bits,entries,presets);
        c.loadPresets();
        for(String value:presets) {
            c.data.palette().idFor(value,(n,v)-> {
                throw new IllegalArgumentException("Unexpected palette resize, bits = "+n+", added value = "+v);
            });
        }
        // Loaded values must remain available without further resizing.
        for (String value : entries) {
            c.data.palette().idFor(value,(n,v)-> {
                throw new AssertionError("Loaded value lost after preset insertion: "+v);
            });
        }
        if(c.data.palette().getSize() > (1 << c.data.configuration().bitsInMemory())) {
            throw new AssertionError("Palette exceeds its storage bit width");
        }
        System.out.println("PASS "+label);
    }
    static List<String> unique(int n) {
        List<String> out=new ArrayList<>();
        for(int i=0;i<n;i++) out.add(("block-"+i).intern());
        return out;
    }
    static List<String> duplicates(int slots,int uniqueCount) {
        List<String> out=unique(uniqueCount);
        while(out.size()<slots) out.add(out.get(0));
        return out;
    }
    public static void main(String[] args) {
        int failures=0;
        List<Runnable> tests=List.of(
            ()->check("duplicate-full-mode1-16-slots-15-states",4,duplicates(16,15),"stone"),
            ()->check("duplicate-full-mode3-64-slots-53-states",6,duplicates(64,53),"coal_ore","gold_ore","redstone_ore"),
            ()->check("duplicate-full-mode1-64-slots-2-states",6,duplicates(64,2),"stone"),
            ()->check("duplicate-full-mode3-64-slots-2-states",6,duplicates(64,2),"coal_ore","gold_ore","redstone_ore"),
            ()->check("duplicate-full-already-present",5,duplicates(32,25),"block-2"),
            ()->check("unique-full-missing-resize",6,unique(64),"stone"),
            ()->check("unique-full-multiple-missing-resize",6,unique(64),"coal_ore","gold_ore","redstone_ore"),
            ()->check("unique-full-already-present",6,unique(64),"block-2"),
            ()->check("nonfull-missing",6,unique(12),"stone"),
            ()->check("nonfull-duplicate-missing",6,duplicates(40,2),"stone")
        );
        for(Runnable test:tests) {
            try { test.run(); }
            catch(IllegalArgumentException e) { System.out.println("FAIL "+e.getMessage()); failures++; }
        }
        if(failures!=0) throw new AssertionError(failures+" palette regression cases failed");
    }
}
'''
with tempfile.TemporaryDirectory() as d:
    file=Path(d)/'PaletteRegression.java'
    file.write_text(java)
    subprocess.run(['javac','-Xlint:unchecked','-d',d,str(file)],check=True)
    result=subprocess.run(['java','-cp',d,'net.minecraft.world.level.chunk.PaletteRegression'])
    raise SystemExit(result.returncode)
