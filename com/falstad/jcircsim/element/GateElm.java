package com.falstad.jcircsim.element;

import com.falstad.jcircsim.view.edit.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

abstract class GateElm extends CircuitElm
{
    public final int FLAG_SMALL = 1;
    public int inputCount = 2;
    public boolean lastOutput;

    public GateElm(int xx, int yy)
    {
        super(xx, yy);
        noDiagonal = true;
        inputCount = 2;
        try
        {
            setSize(sim.smallGridCheckItem.getState() ? 1 : 2);
        }
        catch (Exception e)
        {
            setSize(1);
        }
    }

    public GateElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
    {
        super(xa, ya, xb, yb, f);
        inputCount = Integer.parseInt(st.nextToken());
        lastOutput = Double.parseDouble(st.nextToken()) > 2.5;
        noDiagonal = true;
        setSize((f & FLAG_SMALL) != 0 ? 1 : 2);
    }

    public boolean isInverting()
    {
        return false;
    }

    public int gsize, gwidth, gwidth2, gheight, hs2;

    public void setSize(int s)
    {
        gsize = s;
        gwidth = 7 * s;
        gwidth2 = 14 * s;
        gheight = 8 * s;
        flags = (s == 1) ? FLAG_SMALL : 0;
    }

    public String dump()
    {
        return super.dump() + " " + inputCount + " " + volts[inputCount];
    }

    public Point inPosts[], inGates[];
    public int ww;

    public void setPoints()
    {
        super.setPoints();
        if (dn > 150 && this == sim.dragElm)
            setSize(2);
        int hs = gheight;
        int i;
        ww = gwidth2; // was 24
        if (ww > dn / 2)
            ww = (int) (dn / 2);
        if (isInverting() && ww + 8 > dn / 2)
            ww = (int) (dn / 2 - 8);
        calcLeads(ww * 2);
        inPosts = new Point[inputCount];
        inGates = new Point[inputCount];
        allocNodes();
        int i0 = -inputCount / 2;
        for (i = 0; i != inputCount; i++, i0++)
        {
            if (i0 == 0 && (inputCount & 1) == 0)
                i0++;
            inPosts[i] = interpPoint(point1, point2, 0, hs * i0);
            inGates[i] = interpPoint(lead1, lead2, 0, hs * i0);
            volts[i] = (lastOutput ^ isInverting()) ? 5 : 0;
        }
        hs2 = gwidth * (inputCount / 2 + 1);
        setBbox(point1, point2, hs2);
    }

    public void draw(Graphics g)
    {
        int i;
        for (i = 0; i != inputCount; i++)
        {
            setVoltageColor(g, volts[i]);
            drawThickLine(g, inPosts[i], inGates[i]);
        }
        setVoltageColor(g, volts[inputCount]);
        drawThickLine(g, lead2, point2);
        g.setColor(needsHighlight() ? selectColor : lightGrayColor);
        drawThickPolygon(g, gatePoly);
        if (linePoints != null)
            for (i = 0; i != linePoints.length - 1; i++)
                drawThickLine(g, linePoints[i], linePoints[i + 1]);
        if (isInverting())
            drawThickCircle(g, pcircle.x, pcircle.y, 3);
        curcount = updateDotCount(current, curcount);
        drawDots(g, lead2, point2, curcount);
        drawPosts(g);
    }

    public Polygon gatePoly;
    public Point pcircle, linePoints[];

    public int getPostCount()
    {
        return inputCount + 1;
    }

    public Point getPost(int n)
    {
        if (n == inputCount)
            return point2;
        return inPosts[n];
    }

    public int getVoltageSourceCount()
    {
        return 1;
    }

    public abstract String getGateName();

    public void getInfo(String arr[])
    {
        arr[0] = getGateName();
        arr[1] = "Vout = " + getVoltageText(volts[inputCount]);
        arr[2] = "Iout = " + getCurrentText(getCurrent());
    }

    public void stamp()
    {
        sim.stampVoltageSource(0, nodes[inputCount], voltSource);
    }

    public boolean getInput(int x)
    {
        return volts[x] > 2.5;
    }

    public abstract boolean calcFunction();

    public void doStep()
    {
        int i;
        boolean f = calcFunction();
        if (isInverting())
            f = !f;
        lastOutput = f;
        double res = f ? 5 : 0;
        sim.updateVoltageSource(0, nodes[inputCount], voltSource, res);
    }

    public EditInfo getEditInfo(int n)
    {
        if (n == 0)
            return new EditInfo("# of Inputs", inputCount, 1, 8).
                    setDimensionless();
        return null;
    }

    public void setEditValue(int n, EditInfo ei)
    {
        inputCount = (int) ei.value;
        setPoints();
    }

    // there is no current path through the gate inputs, but there
    // is an indirect path through the output to ground.
    public boolean getConnection(int n1, int n2)
    {
        return false;
    }

    public boolean hasGroundConnection(int n1)
    {
        return (n1 == inputCount);
    }
}

