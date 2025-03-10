/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.common.utils;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Objects;

@Slf4j
public class ImageUtil {
    // Does not resolve the @2x automatically
    public static Image getImageByPath(String path) {
        try (InputStream resourceAsStream = ImageUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (resourceAsStream == null) {
                return null;
            }
            return new Image(Objects.requireNonNull(resourceAsStream));
        } catch (Exception e) {
            log.error("Loading image failed: path={}", path);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Image getImageByPath(String path, int width, int height) {
        try (InputStream resourceAsStream = ImageUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (resourceAsStream == null) {
                return null;
            }
            return new Image(resourceAsStream, width, height, true, true);
        } catch (Exception e) {
            log.error("Loading image failed: path={}", path);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // Does resolve the @2x automatically
    public static ImageView getImageViewById(String id) {
        ImageView imageView = new ImageView();
        imageView.setId(id);
        return imageView;
    }

    // determine if this is a macOS retina display
    // https://stackoverflow.com/questions/20767708/how-do-you-detect-a-retina-display-in-java#20767802
    public static boolean isRetina() {
        return Screen.getPrimary().getOutputScaleX() > 1.5;
    }

    public static void addAppIcons(Stage stage) {
        stage.getIcons().add(ImageUtil.getImageByPath("images/app_window/icon_512.png"));
        stage.getIcons().add(ImageUtil.getImageByPath("images/app_window/icon_256.png"));
        stage.getIcons().add(ImageUtil.getImageByPath("images/app_window/icon_128.png"));
        stage.getIcons().add(ImageUtil.getImageByPath("images/app_window/icon_64.png"));
        stage.getIcons().add(ImageUtil.getImageByPath("images/app_window/icon_32.png"));
        stage.getIcons().add(ImageUtil.getImageByPath("images/app_window/icon_16.png"));
    }

    /**
     * @param size
     * @param cssStrokeColor E.g. -bisq2-green
     */
    public static StackPane addRingToNode(Node node, double size, double strokeWidth, String cssStrokeColor) {
        StackPane pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        Circle circle = new Circle(size / 2);
        circle.setSmooth(true);
        circle.setStrokeWidth(strokeWidth);
        circle.setStrokeType(StrokeType.OUTSIDE);
        circle.setFill(Color.TRANSPARENT);
        pane.getChildren().addAll(node, circle);
        circle.setStyle("-fx-stroke: " + cssStrokeColor);
        return pane;
    }

    public static StackPane getOverlappedIconsPane(String leftIconId, String rightIconId) {
        return getOverlappedIconsPane(leftIconId, rightIconId, 20, "overlapped-icons");
    }

    public static StackPane getOverlappedIconsPane(String leftIconId,
                                                   String rightIconId,
                                                   double size,
                                                   String circleStyle) {
        StackPane pane = new StackPane();
        double paneWidth = size * 2 + 1;
        pane.setMinWidth(paneWidth);
        pane.setMaxWidth(paneWidth);

        Node leftIcon = ImageUtil.getImageViewById(leftIconId);

        Node rightIcon = ImageUtil.getImageViewById(rightIconId);
        double radius = size / 2 + 3;
        Circle circle = new Circle(radius);
        circle.getStyleClass().add(circleStyle);
        StackPane rightIconWithRing = new StackPane();
        rightIconWithRing.setMinWidth(radius * 2);
        rightIconWithRing.setMaxWidth(radius * 2);
        StackPane.setAlignment(circle, Pos.CENTER);
        StackPane.setAlignment(rightIcon, Pos.CENTER);
        rightIconWithRing.getChildren().addAll(circle, rightIcon);

        StackPane.setAlignment(leftIcon, Pos.CENTER_LEFT);
        StackPane.setAlignment(rightIconWithRing, Pos.CENTER_RIGHT);
        pane.getChildren().addAll(leftIcon, rightIconWithRing);
        return pane;
    }

}
