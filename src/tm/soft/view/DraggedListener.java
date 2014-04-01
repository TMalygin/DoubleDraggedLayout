package tm.soft.view;

interface DraggedListener {

	void startDragged();

	void dragged(float delta);

	void stopDragged();
}
