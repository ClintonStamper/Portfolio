
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JPanel;

public class CustomGraphClass extends JPanel implements MouseListener, MouseMotionListener{

	/**
	 * This was a custom class I wrote to integrate into Genetesis's MVP.  The reason that I developed this was because there was no modern looking graph class that I could find.
	 * This was written in the summer of 2015.
	 */
	//Serial number for class
	private static final long serialVersionUID = -8256931073103571746L;

	//Minimum and max values for X to display
	private double minX;
	private double maxX;

	//Minimum and max values for Y to display
	private double minY;
	private double maxY;

	//The actual width of the area in pixels that the graph will have to display information.
	private int width;
	private int height;

	//The amount of units between each major and minor mark for the respective graphs.
	private double XaxisMajorScale = 1000;
	private double XaxisMinorScale = 100;
	private double YaxisMajorScale = 10000;
	private double YaxisMinorScale = 1000;

	//The position of the 0 line for both the y and x access
	private double XAxisPositionInPixels = 0;
	private double YAxisPositionInPixels = 0;

	//The conversion from Y units to Pixels
	private double XPixelsPerUnit = 0;
	private double YPixelsPerUnit = 0;

	private float majorAxisMarksStroke = 0.5f;
	private float minorAxisMarksStroke = 0.5f;
	private float zeroAxisStroke = 1;
	private float lineGraphStroke = 2;

	private Color majorAxisMarksColor = new Color(200,200,200);
	private Color minorAxisMarksColor = new Color(220,220,220);
	private Color zeroAxisColor = new Color(51,51,51);
	private Color lineGraphColor = new Color(102,153,255);

	private double selectedDataPointXcord = 0;
	private double selectedDataPointYcord = 0;

	private double selectedDataPointXvalue = 0;
	private double selectedDataPointYvalue = 0;

	private TreeMap<Double, Double> data = new TreeMap<Double, Double>();
	
	
	private String graphXUnits = "ms";
	private String graphYUnits = "pT";
	


	//Constructors
	/**
	 * Create the panel.
	 */
	public CustomGraphClass(int width, int height, double minX, double maxX, double minY, double maxY) {
		this.width = width;
		this.height = height;
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		generalSetup();
	}

	/**
	 * Create the panel.
	 * @wbp.parser.constructor
	 */
	public CustomGraphClass(double maxX, double maxY) {
		this.width = this.getWidth();
		this.height = this.getHeight();
		this.minX = 0;
		this.minY = 0;
		this.maxX = maxX;
		this.maxY = maxY;
		generalSetup();
	}

	/**
	 * Create the panel.
	 */
	public CustomGraphClass(double minX, double maxX, double minY, double maxY) {
		this.width = this.getWidth();
		this.height = this.getHeight();
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		generalSetup();
	}

	/**
	 * Create the panel.
	 */
	public CustomGraphClass(TreeMap<Double, Double> keyAndValueData) {
		this.width = this.getWidth();
		this.height = this.getHeight();
		this.data = keyAndValueData;
		this.generateMaxAndMin(true, 0, true, 50);
		generalSetup();
	}

	private void generalSetup() {
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	//SET DATA


	public void setData(TreeMap<Double, Double> keyAndValueData){
		this.data = keyAndValueData;
		setZeroPointsAndPointUnits();
	}

	public void setData(double[][] keyAndValueData){
		for(double[] keyAndValue : keyAndValueData){
			double key = keyAndValue[0];
			double value = keyAndValue[1];
			this.data.put(key, value);
		}
		setZeroPointsAndPointUnits();
	}

	public void setDataWithBaseInterval(double[] valueData, int startIndex){
		for(int i = 0 ; i < valueData.length ; i++, startIndex++){
			this.data.put((double)startIndex, valueData[i]);
		}
		setZeroPointsAndPointUnits();
	}

	private void setZeroPointsAndPointUnits(){
		//Setting zero points
		double yDiff = this.maxY - this.minY;
		if(this.minY > 0){
			this.XAxisPositionInPixels = height;
		}else if(this.maxY < 0){
			this.XAxisPositionInPixels = 0;
		}else{
			this.XAxisPositionInPixels = (this.maxY/((double)yDiff) * this.height);
		}

		double xDiff = this.maxX - this.minX;
		if(this.minX > 0){
			this.YAxisPositionInPixels = 0;
		}else if(this.maxX < 0){
			this.YAxisPositionInPixels = this.width;
		}else{
			this.YAxisPositionInPixels = (this.minX/((double)xDiff) * this.width);
		}

		//Setting pixels per unit
		this.XPixelsPerUnit = this.width/xDiff;
		this.YPixelsPerUnit = this.height/yDiff;

	}


	//SET DRAW AXISES


	/**
	 * Generates the axis marks for the given selection.
	 * @param max	The max value possible
	 * @param min	The min value possible
	 * @param major	True if you want major marks, false if you want minor marks
	 * @return	
	 */
	private double[] generateAxisMarks(double max, double min, boolean major, boolean xAxis){
		double diff = max-1 - min;
		double axisMajorScale = xAxis ? XaxisMajorScale : YaxisMajorScale;
		double axisMinorScale = xAxis ? XaxisMinorScale : YaxisMinorScale;
		double axisScale = major ? axisMajorScale : axisMinorScale;
		int numberOfMarks = (int) Math.round(diff/axisScale);
		double[] result = new double[numberOfMarks];
		double currentMark = (((int)(min / axisScale))*axisScale);
		int i = 0;
		while(currentMark < max && i < result.length){
			result[i] = currentMark;
			currentMark += axisScale;
			i++;
		}
		return result;
	}



	//Utility METHODS------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Method to create the max and min for the graph.
	 * @param doX	True if you want to set the max and min of X
	 * @param percentOverShootX	The overshoot of X.
	 * @param doY	True if you want to set the max and min of Y
	 * @param percentOverShootY	The overshoot of Y
	 */
	public void generateMaxAndMin(boolean doX, int percentOverShootX, boolean doY, int percentOverShootY){
		if(doX){
			double maxInXData = getExtreme(true, false, this.data);
			double minInXData = getExtreme(false, false, this.data);
			double XDiff = maxInXData - minInXData;
			double biggerAreaX = XDiff/(1.0 - (percentOverShootX/100.0));
			double differenceInArea = biggerAreaX - XDiff;
			double differenceToAddOnEach = differenceInArea/2;
			this.maxX = (int) (maxInXData + differenceToAddOnEach);
			this.minX = (int) (minInXData - differenceToAddOnEach);
		}
		if(doY){
			double maxInYData = getExtreme(true, true, this.data);
			double minInYData = getExtreme(false, true, this.data);
			double YDiff = maxInYData - minInYData;
			double biggerAreaY = YDiff/(1.0 - (percentOverShootY/100.0));
			double differenceInArea = biggerAreaY - YDiff;
			double differenceToAddOnEach = differenceInArea/2;
			this.maxY = (maxInYData + differenceToAddOnEach);
			this.minY = (minInYData - differenceToAddOnEach);
		}
		setZeroPointsAndPointUnits();
	}

	/**
	 * This method finds the max/min in a given Treemap
	 * @param max	True if you want max, false if you want min.
	 * @param extremeValue	True if you want to use the values, False if you want to use the keys.
	 * @param dataToCheck The map to go through.
	 * @return	The extreme.
	 */
	private static double getExtreme(boolean max, boolean useValues, Map<Double, Double> dataToCheck) {
		double result = max ? Double.MIN_VALUE : Double.MAX_VALUE;
		Set<Double> keys = dataToCheck.keySet();
		for(Double key : keys){
			if(useValues){
				double valueToCheck = dataToCheck.get(key);
				if(max ? valueToCheck > result : valueToCheck < result){
					result = valueToCheck;
				}
			}else{
				if(max ? key > result : key < result){
					result = key;
				}
			}
		}
		return result;
	}


	public void resize(int width, int height){
		if(width != 10){
			this.width = width;
			this.height = height;
			setZeroPointsAndPointUnits();
		}
	}

	public void createAndSetFont(String fontFileName){
		try {
			fontToUse = Font.createFont(Font.TRUETYPE_FONT, new File(fontFileName)).deriveFont(50f);
			//create the font to use. Specify the size!
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			//register the font
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(fontFileName)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch(FontFormatException e)
		{
			e.printStackTrace();
		}

	}



	//Painting METHODS------------------------------------------------------------------------------------------------------------------------------------------------------

	Font fontToUse = null;
	Color fontColor = new Color(200,200,200);
	Color ovalColor = new Color(160,160,160);


	@Override
	public void paint(Graphics g){
		super.paint(g);

		g.setColor(minorAxisMarksColor);
		drawAxisMarks((Graphics2D) g, .50, false, true);
		drawAxisMarks((Graphics2D) g, .50, false, false);

		g.setColor(majorAxisMarksColor);
		drawAxisMarks((Graphics2D) g, .50, true, true);
		drawAxisMarks((Graphics2D) g, .50, true, false);

		g.setColor(zeroAxisColor);
		drawAxises((Graphics2D) g, true, true);

		g.setColor(lineGraphColor);
		drawLineGraph((Graphics2D) g);
		
		drawPointAndValue(g);

	}

	private void drawPointAndValue(Graphics g) {
		g.setColor(ovalColor);
		g.drawOval((int)selectedDataPointXcord-7, (int)selectedDataPointYcord-7, 14, 14);
		if(fontToUse != null){
			g.setFont(fontToUse);
		}
		g.setColor(fontColor);
		g.drawString(String.format("%,.2f " + graphXUnits,selectedDataPointXvalue), 10, 50);
		g.drawString(String.format("%,.2f " + graphYUnits,selectedDataPointYvalue), 10, 110);
	}

	/**
	 * Used to draw the x axis marks, could be changed to do Y in the future.
	 * @param g	The Graphics2D
	 * @param percentToDraw percent of length to draw of height
	 * @param major	True if major axis, false if otherwise
	 */
	private void drawAxisMarks(Graphics2D g, double percentToDraw, boolean major, boolean xAxis){
		
		if(major){
			g.setStroke(new BasicStroke(majorAxisMarksStroke));
		}else{
			g.setStroke(new BasicStroke(minorAxisMarksStroke));
		}
		
		if(xAxis){
			
			
			double currentX = 0;
			int length = (int) (percentToDraw * this.height);
			double[] AxisMarks = generateAxisMarks(this.maxX, this.minX, major, xAxis);
			for(int i = 0 ; i < AxisMarks.length ; i++){
				currentX = projectToGraph(AxisMarks[i], true);
				g.drawLine((int)currentX, 0, (int)currentX, (int)length);
				g.drawLine((int)currentX, this.height, (int)currentX, this.height - length);
			}
			
			
		}else{
			
			
			double currentY = 0;
			int length = (int) (percentToDraw * this.width);
			double[] AxisMarks = generateAxisMarks(this.maxY, this.minY, major, xAxis);
			for(int i = 0 ; i < AxisMarks.length ; i++){
				currentY = projectToGraph(AxisMarks[i], false);
				g.drawLine(0, (int)currentY, length, (int)currentY);
				g.drawLine(this.width, (int)currentY, this.width-length, (int)currentY);
			}
			
			
		}
	}

	private void drawAxises(Graphics2D g, boolean X, boolean Y){
		g.setStroke(new BasicStroke(zeroAxisStroke));
		if(X){
			g.drawLine(0, (int)this.XAxisPositionInPixels, this.width, (int)this.XAxisPositionInPixels);
		}
		if(Y){
			g.drawLine((int)this.YAxisPositionInPixels, 0, (int)this.YAxisPositionInPixels, this.height);
		}
	}

	private void drawLineGraph(Graphics2D g){
		g.setStroke(new BasicStroke(lineGraphStroke));
		Point current = new Point();
		if(!data.isEmpty()){
			double firstKey = data.firstKey();
			Point previous = new Point();
			previous.setLocation(firstKey, (double)data.get(firstKey));
			Set<Double> keys = data.keySet();
			for(double key : keys){
				double value = data.get(key);
				current.setLocation(key, value);
				graphLine(g, previous, current);
				previous.setLocation(current.getX(), current.getY());
			}
		}
	}

	private void graphLine(Graphics2D g, Point previous, Point current) {
		int firstX = projectToGraph(previous.getX(), true);
		int firstY = projectToGraph(previous.getY(), false);
		int nextX = projectToGraph(current.getX(), true);
		int nextY = projectToGraph(current.getY(), false);
		g.drawLine(firstX, firstY, nextX, nextY);
	}

	private int projectToGraph(double graphValue, boolean isXCord) {
		int result = 0;
		double units = isXCord ? this.XPixelsPerUnit : this.YPixelsPerUnit;
		double min = isXCord ? this.minX : this.minY;
		double max = isXCord ? this.maxX : this.maxY;
		double diff = isXCord ? graphValue - min : max - graphValue;
		result += (int)(diff * units);
		return result;
	}	

	private double projectFromGraph(double pixels, boolean isXCord) {
		double result = 0;
		double units = isXCord ? this.XPixelsPerUnit : this.YPixelsPerUnit;
		double min = isXCord ? this.minX : this.minY;
		double max = isXCord ? this.maxX : this.maxY;
		double leftHandResult = pixels/units;
		result = isXCord ? leftHandResult + min : max - leftHandResult;
		return result;
	}	


	private double nearestDataPoint(double dataPoint) {
		double result = 0;
		Set<Double> keys = data.keySet();
		Iterator<Double> iter = keys.iterator();
		if(keys.size() > 2){
			double last = iter.next();
			double current = iter.next();
			while(current < dataPoint && iter.hasNext()){
				last = current;
				current = iter.next();
			}
			double lastDiff = dataPoint - last;
			double totalDiff = current - last;
			double percentBetweenNumbers = Math.abs(lastDiff) / Math.abs(totalDiff);
			double dataDiff = data.get(current) - data.get(last);
			result = dataDiff * percentBetweenNumbers + data.get(last);
		}
		return result;
	}



	//OVERIDE METHODS------------------------------------------------------------------------------------------------------------------------------------------------------



	@Override
	public void setBounds(int x, int y, int width, int height){
		if(x == 0 && y == 0){
			super.setBounds(x, y, width, height);
			resize(width, height);
		}
	}

	@Override
	public void mouseClicked(MouseEvent mouse) {
		moveGraphToPoint(mouse, true);
	}


	@Override
	public void mouseReleased(MouseEvent mouse) {
		moveGraphToPoint(mouse, false);
	}

	@Override
	public void mouseDragged(MouseEvent mouse) {
		moveGraphToPoint(mouse, false);

	}

	public void moveGraphToPoint(MouseEvent mouse, boolean moveAnimation) {
		mouseToGraphConversion(mouse.getX(), moveAnimation);
		paintImmediately(getBounds());
	}
	
	public void moveGraphToPercent(double percentToMoveTo){
		percentToMoveTo *= this.getWidth();
		this.mouseToGraphConversion(percentToMoveTo, false);
		paintImmediately(getBounds());
	}

	private void mouseToGraphConversion(double Xcord, boolean moveAnimation) {
		selectedDataPointXcord = Xcord;

		selectedDataPointXcord = projectFromGraph(selectedDataPointXcord, true);
		selectedDataPointYcord = nearestDataPoint(selectedDataPointXcord);
		selectedDataPointYvalue = selectedDataPointYcord;
		selectedDataPointXvalue = selectedDataPointXcord;
		
		selectedDataPointYcord = projectToGraph(selectedDataPointYcord, false);
		selectedDataPointXcord = projectToGraph(selectedDataPointXcord, true);
	}





































	@Override
	public void mouseMoved(MouseEvent mouse) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent mouse) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent mouse) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent mouse) {

	}




}
