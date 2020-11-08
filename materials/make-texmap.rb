#!/usr/bin/ruby

require 'rubygems'
require 'fileutils'
require 'pathname'
require 'rmagick'

ROW_COMMON = 0
ROW_TILES = 0 # in the game - 6, because common is on top of set textures

BASE_ICONS = ROW_COMMON * 15
BASE_OBJECTS = BASE_ICONS + 10
BASE_BULLETS = BASE_OBJECTS + 19
BASE_EXPLOSIONS = BASE_BULLETS + 4
BASE_ARROWS = BASE_EXPLOSIONS + 3
BASE_WEAPONS = BASE_ARROWS + 4
BASE_BACKS = (ROW_COMMON + 4) * 15

BASE_WALLS = ROW_TILES * 15
BASE_TRANSP_WALLS = BASE_WALLS + 44
# BASE_TRANSP_PASSABLE = BASE_TRANSP_WALLS + 5
BASE_TRANSP_WINDOWS = BASE_TRANSP_WALLS + 9
BASE_DOORS_F = BASE_TRANSP_WINDOWS + 8
BASE_DOORS_S = BASE_DOORS_F + 8
BASE_DECOR_ITEM = BASE_DOORS_S + 8
BASE_DECOR_LAMP = BASE_DECOR_ITEM + 10
BASE_FLOOR = BASE_DECOR_LAMP + 2
BASE_CEIL = BASE_FLOOR + 10

COUNT_MONSTER = 0x10 # block = [up, rt, dn, lt], monster = block[walk_a, walk_b, hit], die[3], shoot

# 225 textures max

class TexMapCreator
    def initialize(src_dir, dst_base, suffix)
        @is_magick_v6 = Magick::IMAGEMAGICK_VERSION.match(/^6\./)

        @src_dir = src_dir
        @dst_base = dst_base
        @suffix = suffix

        @res_dir = @dst_base + '/res'
        @assets_dir = @dst_base + '/assets'
        @textures_dir = @dst_base + "/assets/textures"

        @tile_size = 128
        @mon_tile_size = 256

        @weapons = [
            { :name => 'knife', :frames => 4 },
            { :name => 'pist', :frames => 4 },
            { :name => 'dblpist', :frames => 4 },
            { :name => 'ak47', :frames => 4 },
            { :name => 'tmp', :frames => 4 },
            { :name => 'shtg', :frames => 5 },
            { :name => 'rocket', :frames => 8 },
        ]
    end

    def save_jpeg(img, name)
        img.write(name) { self.interlace = Magick::PlaneInterlace ; self.quality = 100 }
    end

    def save_pixels_alpha(img, pixels_name, alpha_name)
        alpha = img.channel(Magick::AlphaChannel)
        alpha = alpha.negate if @is_magick_v6
        alpha.write(alpha_name)

        list = Magick::ImageList.new
        list.new_image(img.columns, img.rows) { self.background_color = '#000000' }
        list << img
        save_jpeg(list.flatten_images, pixels_name)
    end

    def load_opaque_image(name, width, height)
        puts name

        list = Magick::ImageList.new(name)
        list.set_channel_depth(Magick::AllChannels, 8)
        list.background_color = '#000000' # just for case
        img = list.first

        return img.resize(width, height, Magick::LanczosFilter, 1.0)
    end

    def load_alpha_image(name, width, height, premultiply_alpha=true, background_color='#000000')
        puts name

        list = Magick::ImageList.new(name)
        list.set_channel_depth(Magick::AllChannels, 8)
        list.alpha(Magick::ActivateAlphaChannel)
        list.background_color = background_color
        list.alpha(Magick::BackgroundAlphaChannel)
        img = list.first

        if premultiply_alpha && (img.rows > 128 || img.columns > 128)
            pixels = img

            alpha = pixels.channel(Magick::AlphaChannel)
            alpha = alpha.negate if @is_magick_v6

            alpha.composite!(alpha, 0, 0, Magick::MultiplyCompositeOp)
            alpha.composite!(alpha, 0, 0, Magick::MultiplyCompositeOp)
            alpha.composite!(alpha, 0, 0, Magick::MultiplyCompositeOp)
            alpha.composite!(alpha, 0, 0, Magick::MultiplyCompositeOp)

            pixels.composite!(alpha, 0, 0, Magick::MultiplyCompositeOp)
            pixels.composite!(alpha, 0, 0, Magick::MultiplyCompositeOp)
            pixels.composite!(alpha, 0, 0, Magick::MultiplyCompositeOp)
            pixels.composite!(alpha, 0, 0, Magick::MultiplyCompositeOp)

            img = pixels
            img.composite!(alpha, 0, 0, Magick::CopyAlphaCompositeOp)
        end

        return img.resize(width, height, Magick::LanczosFilter, 1.0)
    end

    def load_texture(result, name, tex, wrap=:normal, width=@tile_size, height=@tile_size, premultiply_alpha=true, cols = 15, tile_size = @tile_size)
        xpos = (tex % cols) * (tile_size + 2)
        ypos = (tex / cols).floor * (tile_size + 2)

        img = load_alpha_image(name, width, height, premultiply_alpha)
        result.composite!(img, xpos + 1, ypos + 1, Magick::CopyCompositeOp)

        if wrap == :flip
            result.view(xpos, ypos, tile_size + 2, tile_size + 2) do |res_view|
                img.view(0, 0, tile_size, tile_size) do |img_view|
                    for i in 0 ... tile_size
                        res_view[0][i + 1] = img_view[0][i]
                        res_view[i + 1][0] = img_view[i][tile_size - 1]
                        res_view[tile_size + 1][i + 1] = img_view[tile_size - 1][i]
                        res_view[i + 1][tile_size + 1] = img_view[i][0]
                    end

                    res_view[0][0] = img_view[0][tile_size - 1]
                    res_view[0][tile_size + 1] = img_view[0][0]
                    res_view[tile_size + 1][0] = img_view[tile_size - 1][tile_size - 1]
                    res_view[tile_size + 1][tile_size + 1] = img_view[tile_size - 1][0]
                end
            end
        elsif wrap == :floor
            result.view(xpos, ypos, tile_size + 2, tile_size + 2) do |res_view|
                img.view(0, 0, tile_size, tile_size) do |img_view|
                    for i in 0 ... tile_size
                        res_view[0][i + 1] = img_view[tile_size - 1][i]
                        res_view[i + 1][0] = img_view[i][tile_size - 1]
                        res_view[tile_size + 1][i + 1] = img_view[0][i]
                        res_view[i + 1][tile_size + 1] = img_view[i][0]
                    end

                    res_view[0][0] = img_view[tile_size - 1][tile_size - 1]
                    res_view[0][tile_size + 1] = img_view[tile_size - 1][0]
                    res_view[tile_size + 1][0] = img_view[0][tile_size - 1]
                    res_view[tile_size + 1][tile_size + 1] = img_view[0][0]
                end
            end
        elsif wrap == :normal
            result.view(xpos, ypos, tile_size + 2, tile_size + 2) do |res_view|
                img.view(0, 0, tile_size, tile_size) do |img_view|
                    for i in 0 ... tile_size
                        res_view[0][i + 1] = img_view[0][i]
                        res_view[i + 1][0] = img_view[i][0]
                        res_view[tile_size + 1][i + 1] = img_view[tile_size - 1][i]
                        res_view[i + 1][tile_size + 1] = img_view[i][tile_size - 1]
                    end

                    res_view[0][0] = img_view[0][0]
                    res_view[0][tile_size + 1] = img_view[0][tile_size - 1]
                    res_view[tile_size + 1][0] = img_view[tile_size - 1][0]
                    res_view[tile_size + 1][tile_size + 1] = img_view[tile_size - 1][tile_size - 1]
                end
            end
        end
    end

    def process_tex_common
        base = "#{@src_dir}/common"
        result = Magick::Image.new((@tile_size + 2) * 15, (@tile_size + 2) * 6).matte_reset!

        load_texture(result, "#{base}/icons/icon_joy.png", BASE_ICONS + 0, :normal, @tile_size, @tile_size, false)
        load_texture(result, "#{base}/icons/icon_menu.png", BASE_ICONS + 1)
        load_texture(result, "#{base}/icons/icon_shoot.png", BASE_ICONS + 2, :normal, @tile_size, @tile_size, false)
        load_texture(result, "#{base}/icons/icon_map.png", BASE_ICONS + 3)
        load_texture(result, "#{base}/icons/icon_health.png", BASE_ICONS + 4)
        load_texture(result, "#{base}/icons/icon_armor.png", BASE_ICONS + 5)
        load_texture(result, "#{base}/icons/icon_ammo.png", BASE_ICONS + 6)
        load_texture(result, "#{base}/icons/icon_blue_key.png", BASE_ICONS + 7)
        load_texture(result, "#{base}/icons/icon_red_key.png", BASE_ICONS + 8)
        load_texture(result, "#{base}/icons/icon_green_key.png", BASE_ICONS + 9)

        load_texture(result, "#{base}/objects/obj_01.png", BASE_OBJECTS + 0)
        load_texture(result, "#{base}/objects/obj_02.png", BASE_OBJECTS + 1)
        load_texture(result, "#{base}/objects/obj_03.png", BASE_OBJECTS + 2)
        load_texture(result, "#{base}/objects/obj_04.png", BASE_OBJECTS + 3)
        load_texture(result, "#{base}/objects/obj_05.png", BASE_OBJECTS + 4)
        load_texture(result, "#{base}/objects/obj_06.png", BASE_OBJECTS + 5)
        load_texture(result, "#{base}/objects/obj_07.png", BASE_OBJECTS + 6)
        load_texture(result, "#{base}/objects/obj_08.png", BASE_OBJECTS + 7)
        load_texture(result, "#{base}/objects/obj_09.png", BASE_OBJECTS + 8)
        load_texture(result, "#{base}/objects/obj_10.png", BASE_OBJECTS + 9)
        load_texture(result, "#{base}/objects/obj_11.png", BASE_OBJECTS + 10)
        load_texture(result, "#{base}/objects/obj_12.png", BASE_OBJECTS + 11)
        load_texture(result, "#{base}/objects/obj_13.png", BASE_OBJECTS + 12)
        load_texture(result, "#{base}/objects/obj_14.png", BASE_OBJECTS + 13)
        load_texture(result, "#{base}/objects/obj_15.png", BASE_OBJECTS + 14)
        load_texture(result, "#{base}/objects/obj_16.png", BASE_OBJECTS + 15)
        load_texture(result, "#{base}/objects/obj_17.png", BASE_OBJECTS + 16)
        load_texture(result, "#{base}/objects/obj_18.png", BASE_OBJECTS + 17)
        load_texture(result, "#{base}/objects/obj_19.png", BASE_OBJECTS + 18)

        load_texture(result, "#{base}/bullets/bull_1_a1.png", BASE_BULLETS + 0)
        load_texture(result, "#{base}/bullets/bull_1_a2.png", BASE_BULLETS + 1)
        load_texture(result, "#{base}/bullets/bull_1_a3.png", BASE_BULLETS + 2)
        load_texture(result, "#{base}/bullets/bull_1_a4.png", BASE_BULLETS + 3)

        load_texture(result, "#{base}/bullets/bull_1_b1.png", BASE_EXPLOSIONS + 0)
        load_texture(result, "#{base}/bullets/bull_1_b2.png", BASE_EXPLOSIONS + 1)
        load_texture(result, "#{base}/bullets/bull_1_b3.png", BASE_EXPLOSIONS + 2)

        load_texture(result, "#{base}/misc/arrow_01.png", BASE_ARROWS + 0)
        load_texture(result, "#{base}/misc/arrow_02.png", BASE_ARROWS + 1)
        load_texture(result, "#{base}/misc/arrow_03.png", BASE_ARROWS + 2)
        load_texture(result, "#{base}/misc/arrow_04.png", BASE_ARROWS + 3)

        @weapons.each_with_index do |item, index|
            load_texture(result, "#{@src_dir}/drawable/weapon_#{item[:name]}.png", BASE_WEAPONS + index)
        end

        load_texture(result, "#{base}/icons/back_joy.png", BASE_BACKS + 0, :nowrap, @tile_size * 2, @tile_size * 2, false)
        load_texture(result, "#{base}/icons/btn_restart.png", BASE_BACKS + 2, :nowrap, @tile_size * 2, @tile_size * 2, false)
        load_texture(result, "#{base}/icons/btn_continue.png", BASE_BACKS + 4, :nowrap, @tile_size * 2, @tile_size * 2, false)

        save_pixels_alpha(result, "#{@textures_dir}/texmap_common_p.jpg", "#{@textures_dir}/texmap_common_a.png")
        # result.write("#{@textures_dir}/texmap_common.png")
    end

    def process_tex_set(set_idx)
        base = "#{@src_dir}/set-#{set_idx}"
        result = Magick::Image.new((@tile_size + 2) * 15, (@tile_size + 2) * 7).matte_reset! # 10 * 66 - max

        load_texture(result, "#{base}/walls/wall_01.png", BASE_WALLS + 0, :flip)
        load_texture(result, "#{base}/walls/wall_02.png", BASE_WALLS + 1)
        load_texture(result, "#{base}/walls/wall_03.png", BASE_WALLS + 2)
        load_texture(result, "#{base}/walls/wall_04.png", BASE_WALLS + 3)
        load_texture(result, "#{base}/walls/wall_05.png", BASE_WALLS + 4, :flip)
        load_texture(result, "#{base}/walls/wall_06.png", BASE_WALLS + 5, :flip)
        load_texture(result, "#{base}/walls/wall_07.png", BASE_WALLS + 6, :flip)
        load_texture(result, "#{base}/walls/wall_08.png", BASE_WALLS + 7, :flip)
        load_texture(result, "#{base}/walls/wall_09.png", BASE_WALLS + 8, :flip)
        load_texture(result, "#{base}/walls/wall_10.png", BASE_WALLS + 9, :flip)
        load_texture(result, "#{base}/walls/wall_11.png", BASE_WALLS + 10, :flip)
        load_texture(result, "#{base}/walls/wall_12.png", BASE_WALLS + 11, :flip)
        load_texture(result, "#{base}/walls/wall_13.png", BASE_WALLS + 12)
        load_texture(result, "#{base}/walls/wall_14.png", BASE_WALLS + 13)
        load_texture(result, "#{base}/walls/wall_15.png", BASE_WALLS + 14)
        load_texture(result, "#{base}/walls/wall_16.png", BASE_WALLS + 15)
        load_texture(result, "#{base}/walls/wall_17.png", BASE_WALLS + 16)
        load_texture(result, "#{base}/walls/wall_18.png", BASE_WALLS + 17)
        load_texture(result, "#{base}/walls/wall_19.png", BASE_WALLS + 18)
        load_texture(result, "#{base}/walls/wall_20.png", BASE_WALLS + 19, :flip)
        load_texture(result, "#{base}/walls/wall_21.png", BASE_WALLS + 20, :flip)
        load_texture(result, "#{base}/walls/wall_22.png", BASE_WALLS + 21, :flip)
        load_texture(result, "#{base}/walls/wall_23.png", BASE_WALLS + 22, :flip)
        load_texture(result, "#{base}/walls/wall_24.png", BASE_WALLS + 23, :flip)
        load_texture(result, "#{base}/walls/wall_25.png", BASE_WALLS + 24, :flip)
        load_texture(result, "#{base}/walls/wall_26.png", BASE_WALLS + 25, :flip)
        load_texture(result, "#{base}/walls/wall_27.png", BASE_WALLS + 26, :flip)
        load_texture(result, "#{base}/walls/wall_28.png", BASE_WALLS + 27, :flip)
        load_texture(result, "#{base}/walls/wall_29.png", BASE_WALLS + 28, :flip)
        load_texture(result, "#{base}/walls/wall_30.png", BASE_WALLS + 29, :flip)
        load_texture(result, "#{base}/walls/wall_31.png", BASE_WALLS + 30, :flip)
        load_texture(result, "#{base}/walls/wall_32.png", BASE_WALLS + 31, :flip)
        load_texture(result, "#{base}/walls/wall_33.png", BASE_WALLS + 32, :flip)
        load_texture(result, "#{base}/walls/wall_34.png", BASE_WALLS + 33, :flip)
        load_texture(result, "#{base}/walls/wall_35.png", BASE_WALLS + 34, :flip)
        load_texture(result, "#{base}/walls/wall_36.png", BASE_WALLS + 35)
        load_texture(result, "#{base}/walls/wall_37.png", BASE_WALLS + 36)
        load_texture(result, "#{base}/walls/wall_38.png", BASE_WALLS + 37)
        load_texture(result, "#{base}/walls/wall_39.png", BASE_WALLS + 38)
        load_texture(result, "#{base}/walls/wall_40.png", BASE_WALLS + 39)
        load_texture(result, "#{base}/walls/wall_41.png", BASE_WALLS + 40)
        load_texture(result, "#{base}/walls/wall_42.png", BASE_WALLS + 41)
        load_texture(result, "#{base}/walls/wall_43.png", BASE_WALLS + 42)
        load_texture(result, "#{base}/walls/wall_44.png", BASE_WALLS + 43)

        load_texture(result, "#{base}/twall/twall_01.png", BASE_TRANSP_WALLS + 0)
        load_texture(result, "#{base}/twall/twall_02.png", BASE_TRANSP_WALLS + 1, :flip)
        load_texture(result, "#{base}/twall/twall_03.png", BASE_TRANSP_WALLS + 2)
        load_texture(result, "#{base}/twall/twall_04.png", BASE_TRANSP_WALLS + 3, :flip)
        load_texture(result, "#{base}/twall/twall_05.png", BASE_TRANSP_WALLS + 4, :flip)
        load_texture(result, "#{base}/twall/twall_06.png", BASE_TRANSP_WALLS + 5)
        load_texture(result, "#{base}/twall/twall_07.png", BASE_TRANSP_WALLS + 6)
        load_texture(result, "#{base}/twall/twall_08.png", BASE_TRANSP_WALLS + 7)
        load_texture(result, "#{base}/twall/twall_09.png", BASE_TRANSP_WALLS + 8)

        # load_texture(result, "#{base}/tpass/tpass_01.png", BASE_TRANSP_PASSABLE + 0, :flip)
        # load_texture(result, "#{base}/tpass/tpass_02.png", BASE_TRANSP_PASSABLE + 1)
        # load_texture(result, "#{base}/tpass/tpass_03.png", BASE_TRANSP_PASSABLE + 2)
        # load_texture(result, "#{base}/tpass/tpass_04.png", BASE_TRANSP_PASSABLE + 3)
        # load_texture(result, "#{base}/tpass/tpass_05.png", BASE_TRANSP_PASSABLE + 4)
        # load_texture(result, "#{base}/tpass/tpass_06.png", BASE_TRANSP_PASSABLE + 5)

        load_texture(result, "#{base}/twind/twind_01.png", BASE_TRANSP_WINDOWS + 0, :flip)
        load_texture(result, "#{base}/twind/twind_02.png", BASE_TRANSP_WINDOWS + 1, :flip)
        load_texture(result, "#{base}/twind/twind_03.png", BASE_TRANSP_WINDOWS + 2, :flip)
        load_texture(result, "#{base}/twind/twind_04.png", BASE_TRANSP_WINDOWS + 3, :flip)
        load_texture(result, "#{base}/twind/twind_05.png", BASE_TRANSP_WINDOWS + 4, :flip)
        load_texture(result, "#{base}/twind/twind_06.png", BASE_TRANSP_WINDOWS + 5, :flip)
        load_texture(result, "#{base}/twind/twind_07.png", BASE_TRANSP_WINDOWS + 6, :flip)
        load_texture(result, "#{base}/twind/twind_08.png", BASE_TRANSP_WINDOWS + 7, :flip)

        load_texture(result, "#{base}/doors/door_01_f.png", BASE_DOORS_F + 0)
        load_texture(result, "#{base}/doors/door_02_f.png", BASE_DOORS_F + 1)
        load_texture(result, "#{base}/doors/door_03_f.png", BASE_DOORS_F + 2)
        load_texture(result, "#{base}/doors/door_04_f.png", BASE_DOORS_F + 3)
        load_texture(result, "#{base}/doors/door_05_f.png", BASE_DOORS_F + 4)
        load_texture(result, "#{base}/doors/door_06_f.png", BASE_DOORS_F + 5)
        load_texture(result, "#{base}/doors/door_07_f.png", BASE_DOORS_F + 6)
        load_texture(result, "#{base}/doors/door_08_f.png", BASE_DOORS_F + 7)

        load_texture(result, "#{base}/doors/door_01_s.png", BASE_DOORS_S + 0)
        load_texture(result, "#{base}/doors/door_02_s.png", BASE_DOORS_S + 1)
        load_texture(result, "#{base}/doors/door_03_s.png", BASE_DOORS_S + 2)
        load_texture(result, "#{base}/doors/door_04_s.png", BASE_DOORS_S + 3)
        load_texture(result, "#{base}/doors/door_05_s.png", BASE_DOORS_S + 4)
        load_texture(result, "#{base}/doors/door_06_s.png", BASE_DOORS_S + 5)
        load_texture(result, "#{base}/doors/door_07_s.png", BASE_DOORS_S + 6)
        load_texture(result, "#{base}/doors/door_08_s.png", BASE_DOORS_S + 7)

        load_texture(result, "#{base}/ditem/ditem_01.png", BASE_DECOR_ITEM + 0, :normal, @tile_size, @tile_size, false)
        load_texture(result, "#{base}/ditem/ditem_02.png", BASE_DECOR_ITEM + 1)
        load_texture(result, "#{base}/ditem/ditem_03.png", BASE_DECOR_ITEM + 2)
        load_texture(result, "#{base}/ditem/ditem_04.png", BASE_DECOR_ITEM + 3)
        load_texture(result, "#{base}/ditem/ditem_05.png", BASE_DECOR_ITEM + 4)
        load_texture(result, "#{base}/ditem/ditem_06.png", BASE_DECOR_ITEM + 5)
        load_texture(result, "#{base}/ditem/ditem_07.png", BASE_DECOR_ITEM + 6)
        load_texture(result, "#{base}/ditem/ditem_08.png", BASE_DECOR_ITEM + 7)
        load_texture(result, "#{base}/ditem/ditem_09.png", BASE_DECOR_ITEM + 8)
        load_texture(result, "#{base}/ditem/ditem_10.png", BASE_DECOR_ITEM + 9, :normal, @tile_size, @tile_size, false)

        load_texture(result, "#{base}/dlamp/dlamp_01.png", BASE_DECOR_LAMP + 0, :normal, @tile_size, @tile_size, false)
        load_texture(result, "#{base}/dlamp/dlamp_02.png", BASE_DECOR_LAMP + 1)

        load_texture(result, "#{base}/floor/floor_01.png", BASE_FLOOR + 0, :floor)
        load_texture(result, "#{base}/floor/floor_02.png", BASE_FLOOR + 1, :floor)
        load_texture(result, "#{base}/floor/floor_03.png", BASE_FLOOR + 2, :floor)
        load_texture(result, "#{base}/floor/floor_04.png", BASE_FLOOR + 3, :floor)
        load_texture(result, "#{base}/floor/floor_05.png", BASE_FLOOR + 4, :floor)
        load_texture(result, "#{base}/floor/floor_06.png", BASE_FLOOR + 5, :floor)
        load_texture(result, "#{base}/floor/floor_07.png", BASE_FLOOR + 6, :floor)
        load_texture(result, "#{base}/floor/floor_08.png", BASE_FLOOR + 7, :floor)
        load_texture(result, "#{base}/floor/floor_09.png", BASE_FLOOR + 8, :floor)
        load_texture(result, "#{base}/floor/floor_10.png", BASE_FLOOR + 9, :floor)

        load_texture(result, "#{base}/ceil/ceil_01.png", BASE_CEIL + 0, :floor)
        load_texture(result, "#{base}/ceil/ceil_02.png", BASE_CEIL + 1, :floor)
        load_texture(result, "#{base}/ceil/ceil_03.png", BASE_CEIL + 2, :floor)
        load_texture(result, "#{base}/ceil/ceil_04.png", BASE_CEIL + 3, :floor)
        load_texture(result, "#{base}/ceil/ceil_05.png", BASE_CEIL + 4, :floor)
        load_texture(result, "#{base}/ceil/ceil_06.png", BASE_CEIL + 5, :floor)

        save_pixels_alpha(result, "#{@textures_dir}/texmap_#{set_idx}_p.jpg", "#{@textures_dir}/texmap_#{set_idx}_a.png")
        # result.write("#{@textures_dir}/texmap_#{set_idx}.png")
    end

    def process_tex_monsters(set, from, to)
        base = "#{@src_dir}/common"
        mts = @mon_tile_size

        result = Magick::Image.new(
            (@mon_tile_size + 2) * 7,
            (((to - from + 1) * 16).to_f / 7.0).ceil.to_i * (@mon_tile_size + 2)
        ).matte_reset!

        (from .. to).each do |idx|
            base_tex = (idx - from) * 16

            load_texture(result, "#{base}/monsters/mon_0#{idx}_a1.png", base_tex + 0, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_a2.png", base_tex + 1, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_a3.png", base_tex + 2, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_a4.png", base_tex + 3, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_b1.png", base_tex + 4, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_b2.png", base_tex + 5, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_b3.png", base_tex + 6, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_b4.png", base_tex + 7, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_c1.png", base_tex + 8, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_c2.png", base_tex + 9, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_c3.png", base_tex + 10, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_c4.png", base_tex + 11, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_d1.png", base_tex + 12, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_d2.png", base_tex + 13, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_d3.png", base_tex + 14, :nowrap, mts, mts, true, 7, mts)
            load_texture(result, "#{base}/monsters/mon_0#{idx}_e.png",  base_tex + 15, :nowrap, mts, mts, true, 7, mts)
        end

        save_pixels_alpha(result, "#{@textures_dir}/texmap_mon_#{set}_p.jpg", "#{@textures_dir}/texmap_mon_#{set}_a.png")
        # result.write("#{@textures_dir}/texmap_mon_#{set}.png")
    end

    def process_tex_hit
        base = "#{@src_dir}/common/hit"

        @weapons.each do |item|
            item[:frames].times do |frame|
                result = load_alpha_image("#{base}/hit_#{item[:name]}_#{frame + 1}.png", 512, 256)

                save_pixels_alpha(
                    result,
                    "#{@textures_dir}/hit_#{item[:name]}_#{frame + 1}_p.jpg",
                    "#{@textures_dir}/hit_#{item[:name]}_#{frame + 1}_a.png"
                )

                # result.write("#{@textures_dir}/hit_#{item[:name]}_#{frame + 1}.png")
            end
        end
    end

    def process_tex_other
        base = "#{@src_dir}/drawable"

        save_jpeg(load_alpha_image("#{base}/sky_1.png", 256, 256, true, '#46a7f1'), "#{@textures_dir}/sky_1.jpg")
        save_jpeg(load_alpha_image("#{base}/tex_loading.png", 256, 256), "#{@textures_dir}/tex_loading.jpg")
    end

    def process_drawables
        base = "#{@src_dir}/drawable"

        # load_alpha_image("#{base}/ic_launcher.png", 48, 48).write("#{@res_dir}/drawable-mdpi#{@suffix}/ic_launcher.png")
        # load_alpha_image("#{base}/ic_launcher.png", 72, 72).write("#{@res_dir}/drawable-hdpi#{@suffix}/ic_launcher.png")
        # load_alpha_image("#{base}/ic_launcher.png", 96, 96).write("#{@res_dir}/drawable-xhdpi#{@suffix}/ic_launcher.png")

        @weapons.each do |item|
            load_alpha_image("#{base}/weapon_#{item[:name]}.png", 80, 80).write("#{@res_dir}/drawable-mdpi#{@suffix}/weapon_#{item[:name]}.png")
        end

        # --------------------------------------------------

        base = "#{@src_dir}/drawable-misc"

        load_alpha_image("#{base}/btn_play_normal_en.png", 156, 84, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_play_normal.png")
        load_alpha_image("#{base}/btn_play_pressed_en.png", 156, 84, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_play_pressed.png")

        load_alpha_image("#{base}/btn_play_normal_ru.png", 156, 84, false).write("#{@res_dir}/drawable-ru-hdpi#{@suffix}/btn_play_normal.png")
        load_alpha_image("#{base}/btn_play_pressed_ru.png", 156, 84, false).write("#{@res_dir}/drawable-ru-hdpi#{@suffix}/btn_play_pressed.png")

        load_alpha_image("#{base}/btn_options_normal.png", 128, 128, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_options_normal.png")
        load_alpha_image("#{base}/btn_options_pressed.png", 128, 128, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_options_pressed.png")

        load_alpha_image("#{base}/btn_like_vk_normal.png", 128, 128, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_like_vk_normal.png")
        load_alpha_image("#{base}/btn_like_vk_pressed.png", 128, 128, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_like_vk_pressed.png")

        load_alpha_image("#{base}/btn_like_facebook_normal.png", 128, 128, false)
            .write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_like_facebook_normal.png")

        load_alpha_image("#{base}/btn_like_facebook_pressed.png", 128, 128, false)
            .write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_like_facebook_pressed.png")

        load_alpha_image("#{base}/btn_like_telegram_normal.png", 128, 128, false)
            .write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_like_telegram_normal.png")

        load_alpha_image("#{base}/btn_like_telegram_pressed.png", 128, 128, false)
            .write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_like_telegram_pressed.png")

        load_alpha_image("#{base}/btn_achievements_normal.png", 128, 128, false)
            .write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_achievements_normal.png")

        load_alpha_image("#{base}/btn_achievements_pressed.png", 128, 128, false)
            .write("#{@res_dir}/drawable-hdpi#{@suffix}/btn_achievements_pressed.png")

        save_jpeg(load_opaque_image("#{base}/back_common.png", 800, 480), "#{@res_dir}/drawable-hdpi#{@suffix}/back_common.jpg")
        load_opaque_image("#{base}/back_splash.png", 800, 480).write("#{@res_dir}/drawable-hdpi#{@suffix}/back_splash.png")

        load_alpha_image("#{base}/ic_like_normal.png", 150, 150, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/ic_like_normal.png")
        load_alpha_image("#{base}/ic_like_pressed.png", 150, 150, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/ic_like_pressed.png")
        load_alpha_image("#{base}/ic_dislike_normal.png", 150, 150, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/ic_dislike_normal.png")
        load_alpha_image("#{base}/ic_dislike_pressed.png", 150, 150, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/ic_dislike_pressed.png")

        load_alpha_image("#{base}/plate.png", 327, 314, false).write("#{@res_dir}/drawable-hdpi#{@suffix}/plate.png")

        load_alpha_image("#{base}/map_cell_hl.png", 40, 30, false).write("#{@res_dir}/drawable-nodpi#{@suffix}/map_cell_hl.png")
        load_alpha_image("#{base}/map_cell.png", 40, 30, false).write("#{@res_dir}/drawable-nodpi#{@suffix}/map_cell.png")
        load_alpha_image("#{base}/map_conn_hor.png", 80, 30, false).write("#{@res_dir}/drawable-nodpi#{@suffix}/map_conn_hor.png")
        load_alpha_image("#{base}/map_conn_vert.png", 40, 60, false).write("#{@res_dir}/drawable-nodpi#{@suffix}/map_conn_vert.png")

        # button_normal.9
        # button_pressed.9
        # button_disabled.9
    end

    def update_cache_version
        File.open("#{@assets_dir}/settings.properties", 'wb') { |fo| fo << "textures_cache_version=#{Time.now.to_i}\n" }
    end

    def optimize
        `pushd "#{@textures_dir}" ; optipng -strip all -o7 *.png ; popd`

        # Do **not** run jpegtran, because results is actually worse
        # find . -type f -name '*.jpg' -exec jpegtran -copy none -optimize -outfile {} {} \\;
    end

    def encode
        Dir.open(@textures_dir).each do |path|
            next unless path =~ /\.(?:png|jpg)$/

            data = File.read("#{@textures_dir}/#{path}").bytes

            (0 ... data.size).each do |idx|
                data[idx] = data[idx] ^ 0xAA
            end

            File.open("#{@textures_dir}/#{Pathname(path).sub_ext('')}.tex", 'wb') { |fo| fo << data.map { |v| v.chr } .join }
            File.delete("#{@textures_dir}/#{path}")
        end
    end
end

def process(mode)
    if mode == 'builder'
        raise RuntimeError.new('This script must be corrected so that it can be used in "builder" mode.')

        src_dir = '<PROJECTS DIR>/projects/fsr-mapper/graphics'
        dst_base = '<PROJECTS DIR>/projects/fsr-mapper/build/.srvbuild'
        suffix = '-v4'
    else
        src_dir = File.dirname(__FILE__) + '/graphics'
        dst_base = File.dirname(__FILE__) + '/../android/src/main'
        suffix = nil
    end

    FileUtils.mkdir_p("#{dst_base}/res/drawable-nodpi#{suffix}")
    FileUtils.mkdir_p("#{dst_base}/res/drawable-mdpi#{suffix}")
    FileUtils.mkdir_p("#{dst_base}/res/drawable-hdpi#{suffix}")
    FileUtils.mkdir_p("#{dst_base}/res/drawable-ru-hdpi#{suffix}")
    # FileUtils.mkdir_p("#{dst_base}/res/drawable-xhdpi#{suffix}")
    FileUtils.mkdir_p("#{dst_base}/assets/textures")

    tmc = TexMapCreator.new(src_dir, dst_base, suffix)

    tmc.process_tex_common
    tmc.process_tex_set(1)
    tmc.process_tex_monsters(1, 1, 3)
    tmc.process_tex_monsters(2, 4, 5)
    tmc.process_tex_hit
    tmc.process_tex_other
    tmc.process_drawables
    tmc.update_cache_version

    if mode == 'release'
        tmc.optimize
    end

    tmc.encode
    puts "Finished creating texmap"
end

# Usage: ruby make-texmap.rb -- for dev
#        ruby make-texmap.rb release -- for release
#        ruby make-texmap.rb builder -- for server

process(ARGV.first)
