package com.crashinvaders.texturepackergui.controllers.main.inputfiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.services.model.InputFile;
import com.crashinvaders.texturepackergui.utils.Scene2dUtils;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.LmlParser;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

class SourceFileSetAdapter extends ArrayAdapter<InputFile, VisTable> {

    private final LmlParser lmlParser;

    public SourceFileSetAdapter(LmlParser lmlParser) {
        super(new Array<InputFile>());
        this.lmlParser = lmlParser;

        setSelectionMode(SelectionMode.MULTIPLE);
        setItemsSorter(new SourceFileComparator());
    }

    public InputFile getSelected() {
        Array<InputFile> selection = getSelectionManager().getSelection();
        if (selection.size == 0) return null;
        return selection.first();
    }

    public ViewHolder getViewHolder(InputFile item) {
        if (indexOf(item) == -1) return null;

        return (ViewHolder) getView(item).getUserObject();
    }

    public ViewHolder getViewHolder(Actor view) {
        ViewHolder viewHolder = (ViewHolder) view.getUserObject();
        if (viewHolder == null) {
            throw new IllegalArgumentException("View has no associated ViewHolder. Probably it's not in adapter yet. View: " + view);
        }
        return viewHolder;
    }

    public boolean isSelected(ViewHolder viewHolder) {
        for (InputFile inputFile : getSelection()) {
            if (viewHolder.getInputFile() == inputFile) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected VisTable createView(InputFile item) {
        ViewHolder viewHolder = new ViewHolder(lmlParser.getData().getDefaultSkin(), item);
        lmlParser.createView(viewHolder, Gdx.files.internal("lml/inputFileListItem.lml"));
        viewHolder.root.setUserObject(viewHolder);
        return viewHolder.root;
    }

    @Override
    protected void prepareViewBeforeAddingToTable(InputFile item, VisTable view) {
        super.prepareViewBeforeAddingToTable(item, view);
    }

    @Override
    protected void selectView(VisTable view) {
        ViewHolder viewHolder = (ViewHolder) view.getUserObject();
        viewHolder.setSelected(true);
    }

    @Override
    protected void deselectView(VisTable view) {
        ViewHolder viewHolder = (ViewHolder) view.getUserObject();
        viewHolder.setSelected(false);
    }

    public static class ViewHolder {
        @LmlActor("root") VisTable root;
        @LmlActor("lblName") VisLabel lblName;
        @LmlActor("imgTypeIndicator") Image imgTypeIndicator;

        private final Skin skin;
        private final InputFile inputFile;
        private final Tooltip tooltip;

        private boolean selected = false;
        private boolean pathProcessed = false;

        public ViewHolder(Skin skin, InputFile inputFile) {
            this.skin = skin;
            this.inputFile = inputFile;

            tooltip = new Tooltip();
            tooltip.setAppearDelayTime(0.25f);
        }

        @LmlAfter void initView() {
            root.pack();
            updateViewData();
        }

        public void remapData() {
            pathProcessed = false;
            updateViewData();
        }

        public void updateViewData() {
            processPathText();

            String imgName = "custom/ic-fileset-";
            if (inputFile.isDirectory()) {
                imgName += "dir";
            } else {
                imgName += "file";
            }
            switch (inputFile.getType()) {
                case Input:
                    break;
                case Ignore:
                    imgName += "-ignore";
                    break;
            }
            imgTypeIndicator.setDrawable(skin.getDrawable(imgName));
        }

        public void setSelected(boolean selected) {
            if (this.selected == selected) return;
            this.selected = selected;

            root.setBackground(selected ? skin.getDrawable("padded-list-selection") : null);
        }

        public InputFile getInputFile() {
            return inputFile;
        }

        private void processPathText() {
            if (pathProcessed) return;
            pathProcessed = true;

            boolean fileShortened = false;
            String origFilePath = inputFile.getFileHandle().path();
            String filePath = origFilePath;

            filePath = Scene2dUtils.ellipsisFilePath(filePath, lblName.getStyle().font, lblName.getWidth());
            fileShortened = !origFilePath.equals(filePath);
            filePath = Scene2dUtils.colorizeFilePath(filePath, inputFile.getFileHandle().isDirectory(), "light-grey", "white");

            lblName.setText(filePath);

            // Show tooltip only if displayed file name was shortened
            tooltip.setTarget(fileShortened ? lblName : null);
            tooltip.setText(Scene2dUtils.colorizeFilePath(origFilePath, inputFile.getFileHandle().isDirectory(), "light-grey", "white"));
        }
    }
}