package org.sprite.splitter;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class Windows {

	protected Shell shell;
	private Canvas canvas;
	private ScrolledComposite scrolledComposite;
	private MenuItem mntmLoadFile;
	private Display display;
	private Image image;
	private float zoom = 1f;
	protected List<Rectangle> boundingBoxes;
	protected Rectangle previousRect;
	protected Rectangle selectedRect;
	protected String filename;
	protected boolean addMode;
	protected Rectangle addRect;
	private static float MIN_ZOOM = 0.3f;
	private static float MAX_ZOOM = 3f;


	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Windows window = new Windows();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(800, 680);
		shell.setText("SWT Application");

		scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(0, 0, 800, 680);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		Menu menu = new Menu(scrolledComposite);
		scrolledComposite.setMenu(menu);

		SelectionAdapter generateBoundBoxListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateBackgroundColor();
				boundingBoxes = generateBoundingBoxes(image);
				canvas.redraw();
			}
		};
		SelectionAdapter savePlistListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (boundingBoxes != null && image != null) {
					try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename.substring(0, filename.lastIndexOf(".")).concat(".plist"))))) {
						writer.print(generateManifest());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		};
		SelectionAdapter openFileListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[]{"*.png", "*.jpg"});
				String selected = fd.open();
				if (null != selected) {
					filename = selected;
					image = new Image(display, selected);
					canvas.setBounds(image.getBounds());
					canvas.redraw();
				}
			}
		};
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText("Load File");
		menuItem.addSelectionListener(openFileListener);
		
		MenuItem menuItem_1 = new MenuItem(menu, SWT.NONE);
		menuItem_1.setText("Generate BoundingBox");
		menuItem_1.addSelectionListener(generateBoundBoxListener);
		
		MenuItem menuItem_2 = new MenuItem(menu, SWT.NONE);
		menuItem_2.setText("Save plist");
		menuItem_2.addSelectionListener(savePlistListener);
		
		canvas = new Canvas(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(canvas);
		scrolledComposite.setMinSize(canvas.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Menu menu_1 = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu_1);
		
		MenuItem mntmFile = new MenuItem(menu_1, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_3 = new Menu(mntmFile);
		mntmFile.setMenu(menu_3);
		
		mntmLoadFile = new MenuItem(menu_3, SWT.NONE);
		mntmLoadFile.setText("Load File");
		
		MenuItem mntmGenerateBoundingbox = new MenuItem(menu_3, SWT.NONE);
		mntmGenerateBoundingbox.addSelectionListener(generateBoundBoxListener);
		mntmGenerateBoundingbox.setText("Generate BoundingBox");
		
		MenuItem mntmSavePlist = new MenuItem(menu_3, SWT.NONE);
		mntmSavePlist.addSelectionListener(savePlistListener);
		mntmSavePlist.setText("Save plist");
		mntmLoadFile.addSelectionListener(openFileListener);
		
		MenuItem mntmHelp = new MenuItem(menu_1, SWT.CASCADE);
		mntmHelp.setText("Help");
		
		Menu menu_2 = new Menu(mntmHelp);
		mntmHelp.setMenu(menu_2);
		
		MenuItem mntmManual = new MenuItem(menu_2, SWT.NONE);
		mntmManual.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ManualDialog dlg = new ManualDialog(shell);
				dlg.open();
			}
		});
		mntmManual.setText("Manual");
		
		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				Transform trans = new Transform(display);
				trans.scale(zoom, zoom);
				if (image != null) {
					e.gc.setTransform(trans);
					e.gc.drawImage(image, 0, 0);
				}
				if (boundingBoxes != null) {
					e.gc.setForeground(new Color(display, 255, 0, 0));
					e.gc.setFillRule(SWT.FILL_WINDING);
					boundingBoxes.forEach(e.gc::drawRectangle);
					if (addRect != null) {
						e.gc.setForeground(new Color(display, 128, 128, 0));
						e.gc.drawRectangle(addRect);
					} else if (previousRect != null) {
						e.gc.setForeground(new Color(display, 0, 255, 0));
						e.gc.drawRectangle(previousRect);
					} else if (selectedRect != null) {
						e.gc.setForeground(new Color(display, 0, 0, 255));
						e.gc.drawRectangle(selectedRect);
					}
				}
			}
			
		});

		shell.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				boolean validate = true;
				switch (Character.toString(e.character)) {
				case "+":
					zoom = zoom * 1.1f;
					if (zoom > MAX_ZOOM) {
						zoom = MAX_ZOOM;
					}
					break;
				case "-":
					zoom = zoom / 1.1f;
					if (zoom < MIN_ZOOM) {
						zoom = MIN_ZOOM;
					}
					break;
				case "0":
					zoom = 1f;
					break;
				default:
					validate = false;
				}
				if (!validate) {
					boolean shift = (e.stateMask & SWT.SHIFT) != 0;
					boolean alt = (e.stateMask & SWT.ALT) != 0;
					validate = true;
					switch (e.keyCode) {
					case SWT.ARROW_UP:
						moveSelectedY(shift, alt, -1);
						break;
					case SWT.ARROW_DOWN:
						moveSelectedY(shift, alt, 1);
						break;
					case SWT.ARROW_LEFT:
						moveSelectedX(shift, alt, -1);
						break;
					case SWT.ARROW_RIGHT:
						moveSelectedX(shift, alt, 1);
						break;
					default:
						validate = false;
					}
				}
				if (!validate) {
					if (e.keyCode == SWT.BS || e.keyCode == SWT.DEL) {
						if (selectedRect != null && boundingBoxes != null) {
							boundingBoxes.remove(selectedRect);
							selectedRect = null;
							validate = true;
						}
					}
				}
				if (validate) {
					canvas.redraw();
				}

			}

			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
		});
		shell.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event) {
				Rectangle rect = shell.getClientArea();
				scrolledComposite.setBounds(rect);
			}

		});
		canvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if (addMode && (e.stateMask & SWT.BUTTON_MASK) != 0) {
					addRect.add(new Rectangle(e.x, e.y, 1, 1));
					canvas.redraw();
				} else {
					addMode = false;
					addRect = null;
					if ((e.stateMask & SWT.BUTTON_MASK) != 0 && boundingBoxes != null) {
						Rectangle rect = rectContains(e);
						if (null != previousRect && previousRect != rect && rect != null) {
							boundingBoxes.remove(rect);
							previousRect.add(rect);
							canvas.redraw();
						} else {
							if (rect != null) {
								previousRect = rect;
							}
						}
					}
				}
				
			}
			
		});
		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (boundingBoxes != null && image != null) {
					Rectangle selected = rectContains(e);
					boundingBoxes.remove(selected);
					if (selected != null)  {
						Image newImage = getSubImage(image, selected);
						List<Rectangle> rects = generateBoundingBoxes(newImage);
						final Rectangle selectedRect = selected;
						rects.forEach(rect -> {
							rect.x = rect.x + selectedRect.x;
							rect.y = rect.y + selectedRect.y;
						});
						boundingBoxes.addAll(rects);
						newImage.dispose();
						canvas.redraw();
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				selectedRect = rectContains(e);
				if (selectedRect == null) {
					addMode = true;
					addRect = new Rectangle(e.x, e.y, 1, 1);
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (addMode && boundingBoxes != null && addRect != null) {
					if (addRect.width > 1 && addRect.height > 1) {
						boundingBoxes.add(addRect);
					}
				} else if (previousRect == null) {
					Rectangle rect = rectContains(e);
					if (selectedRect != rect) {
						selectedRect = null;
					}
				}
				addMode = false;
				addRect = null;
				previousRect = null;

				canvas.redraw();
			}
			
		});
	}

	protected String generateManifest() {
		AtomicInteger integer = new AtomicInteger(0);
		return ManifestGenerator.generate(image, boundingBoxes,
				filename.substring(filename.lastIndexOf(File.separator) + 1),
				() -> String.valueOf(integer.getAndIncrement()) + ".png");
	}

	protected Image getSubImage(Image curImage, Rectangle rect) {
		Image newImage = new Image(display, rect.width, rect.height);
		GC gc = new GC(newImage);
		gc.drawImage(curImage, rect.x, rect.y, rect.width, rect.height, 0, 0, rect.width, rect.height);
		gc.dispose();
		return newImage;
	}

	private void updateBackgroundColor() {
		ImageData imageData = image.getImageData();
		int rgb = imageData.getPixel(0, 0);
		int[] lineData = new int[imageData.width];
		for (int y = 0; y < imageData.height; y++) {
			imageData.getPixels(0, y, lineData.length, lineData, 0);
			for (int x = 0; x < lineData.length; x++) {
				if (lineData[x] == rgb) {
					lineData[x] = 0;
				}
			}
			imageData.setPixels(0, y, lineData.length, lineData, 0);
		}
		image = new Image(display, imageData);
		canvas.redraw();
	}

	List<Rectangle> generateBoundingBoxes(Image curImage) {
		List<Rectangle> rects = new ArrayList<Rectangle>();
		
		ImageData imageData = curImage.getImageData();
		int[][] linesData = new int[imageData.height][];
		byte[][] alphasData = new byte[imageData.height][];
		boolean[] yScan = new boolean[imageData.height];
		boolean[] xScan = new boolean[imageData.width];

		for (int i = 0; i < yScan.length; i++) yScan[i] = true;

		for (int y = 0; y < imageData.height; y++) {
			linesData[y] = new int[imageData.width];
			alphasData[y] = new byte[imageData.width];
			imageData.getPixels(0, y, linesData[y].length, linesData[y], 0);
			imageData.getAlphas(0, y, alphasData[y].length, alphasData[y], 0);
			for (int x = 0; x < linesData[y].length; x++) {
				if (linesData[y][x] != 0) {
					yScan[y] = false;
				}
			}
		}
		List<Range> yRanges = new ArrayList<Range>();
		int index = 0;
		while (index < yScan.length) {
			Range range = findNextRange(yScan, index);
			if (range == null)
				break;
			yRanges.add(range);
			index = range.start + range.length + 1;
		}

		for (Range yRange : yRanges) {
			for (int i = 0; i < xScan.length; i++) xScan[i] = true;
			loop: for (int x = 0; x < xScan.length; x++) {
				for (int y = 0; y < yRange.length; y++) {
					if (linesData[y + yRange.start][x] != 0)  {
						xScan[x] = false;
						continue loop;
					}
				}
			}
			List<Range> xRanges = new ArrayList<Range>();
			index = 0;
			while(index < xScan.length) {
				Range range = findNextRange(xScan, index);
				if (range == null)
					break;
				xRanges.add(range);
				index = range.start + range.length + 1;
			}
			rects.addAll(combineRanges(Arrays.asList(yRange), xRanges));
		}
		return rects;
	}

	private List<Rectangle> combineRanges(List<Range> yRanges, List<Range> xRanges) {
		List<Rectangle> rects = new ArrayList<Rectangle>();
		for(Range y : yRanges) {
			for (Range x : xRanges) {
				rects.add(new Rectangle(x.start, y.start, x.length, y.length));
			}
		}
		return rects;
	}

	class Range {
		int start = 0;
		int length = 0;
		Range(int start, int length) {
			this.start = start;
			this.length = length;
		}
	}

	Range findNextRange(boolean[] data, int start) {
		if (start < 0 || start >= data.length) {
			throw new IndexOutOfBoundsException("Invalid");
		}
		int cur = start;
		while (cur < data.length && data[cur]) cur++;
		int newStart = cur;
		while (cur < data.length && !data[cur]) cur++;
		if (newStart < data.length && cur < data.length && newStart < cur) {
			return new Range(newStart, cur - newStart);
		} else {
			return null;
		}
	}

	private Rectangle rectContains(MouseEvent e) {
		Rectangle selected = null;
		if (boundingBoxes == null)
			return null;
		for (Rectangle rect : boundingBoxes) {
			if (rect.contains(e.x, e.y)) {
				selected = rect;
				break;
			}
		}
		return selected;
	}

	private void moveSelectedY(boolean shift, boolean alt, int by) {
		if (selectedRect != null) {
			if (shift) {
				selectedRect.y = selectedRect.y + by;
				selectedRect.height = selectedRect.height - by;
			} else if (alt) {
				selectedRect.height = selectedRect.height + by;
			} else {
				selectedRect.y = selectedRect.y + by;
			}
		}
	}

	private void moveSelectedX(boolean shift, boolean alt, int by) {
		if (selectedRect != null) {
			if (shift) {
				selectedRect.x = selectedRect.x + by;
				selectedRect.width = selectedRect.width - by;
			} else if (alt) {
				selectedRect.width = selectedRect.width + by;
			} else {
				selectedRect.x = selectedRect.x + by;
			}
		}
	}
}
